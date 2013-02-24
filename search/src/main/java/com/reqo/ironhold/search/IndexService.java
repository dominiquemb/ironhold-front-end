package com.reqo.ironhold.search;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;

import com.reqo.ironhold.search.model.IndexedMailMessage;
import com.reqo.ironhold.search.model.IndexedObjectType;

public class IndexService {
	private static final int RETRY_SLEEP = 10000;
	private static final int MAX_RETRY_COUNT = 10;

	private static Logger logger = Logger.getLogger(IndexService.class);
	private Client esClient;
	private final String indexPrefix;
	private String[] esHosts;
	private int esPort;
	private Set<String> indexes;

	public IndexService(String indexPrefix) throws Exception {
		this.indexPrefix = indexPrefix;

		Properties prop = new Properties();
		prop.load(IndexService.class
				.getResourceAsStream("elasticsearch.properties"));

		esHosts = prop.getProperty("hosts").split(",");
		esPort = Integer.parseInt(prop.getProperty("port"));

		indexes = Collections.synchronizedSet(new HashSet<String>());

		reconnect();

	}

	protected IndexService(String indexPrefix, Client esClient) {
		this.indexPrefix = indexPrefix;
		this.esClient = esClient;

		indexes = Collections.synchronizedSet(new HashSet<String>());
	}

	private void reconnect() throws Exception {
		if (esClient != null) {
			esClient.close();
		}
		esClient = new TransportClient();

		for (String esHost : esHosts) {
			((TransportClient) esClient)
					.addTransportAddress(new InetSocketTransportAddress(esHost,
							esPort));
		}

	}

	private void createIndex(String year) throws Exception {
		String indexName = indexPrefix + "." + year;
		String analyzerDef = readJsonDefinition("analyzers.json");
		CreateIndexResponse response1 = esClient.admin().indices()
				.prepareCreate(indexName).setSettings(analyzerDef).execute()
				.actionGet();
		if (!response1.acknowledged()) {
			throw new Exception("ES Request did not get acknowledged: "
					+ response1.toString());
		}

		String messageDef = readJsonDefinition("message.json");
		PutMappingResponse response3 = esClient.admin().indices()
				.preparePutMapping(indexName)
				.setType(IndexedObjectType.MIME_MESSAGE.getValue())
				.setSource(messageDef).execute().actionGet();
		if (!response3.acknowledged()) {
			throw new Exception("ES Request did not get acknowledged: "
					+ response3.toString());
		}

		String mimeMessageDef = readJsonDefinition("mimeMessage.json");
		PutMappingResponse response4 = esClient.admin().indices()
				.preparePutMapping(indexName)
				.setType(IndexedObjectType.PST_MESSAGE.getValue())
				.setSource(mimeMessageDef).execute().actionGet();
		if (!response4.acknowledged()) {
			throw new Exception("ES Request did not get acknowledged: "
					+ response4.toString());
		}

		IndicesAliasesResponse response5 = esClient.admin().indices()
				.prepareAliases().addAlias(indexName, indexPrefix).execute()
				.actionGet();
		if (!response5.acknowledged()) {
			throw new Exception("ES Request did not get acknowledged: "
					+ response5.toString());
		}
	}

	public boolean store(IndexedMailMessage message) throws Exception {
		String indexName = indexPrefix + "." + message.getYear();

		createIndexIfMissing(message.getYear());

		if (exists(indexName, message.getMessageId(), message.getType())) {
			DeleteResponse response = esClient.prepareDelete(indexName, message.getType().getValue(),
					message.getMessageId()).execute().actionGet();
			logger.info(String.format("Deleting document with id [%s] => %s",
					message.getMessageId(), response.isNotFound()));

		}

		long started = System.currentTimeMillis();
		try {
			int retry = 1;
			boolean success = false;
			while (!success && retry <= MAX_RETRY_COUNT) {
				try {

					logger.debug("Trying to index " + message.getMessageId());

					esClient.prepareIndex(indexName,
							message.getType().getValue(),
							message.getMessageId())
							.setSource(IndexedMailMessage.toJSON(message))
							.execute().actionGet();

					logger.debug("Returned from ES");
					success = true;
					return true;
				} catch (NoNodeAvailableException e) {
					logger.warn("Recieved no node available exception, sleep for "
							+ RETRY_SLEEP
							+ " (Attempt "
							+ retry
							+ " out of "
							+ MAX_RETRY_COUNT + ")");
					success = false;
					retry++;

					try {
						Thread.sleep(RETRY_SLEEP);
					} catch (InterruptedException ignore) {
					}
					reconnect();
				} finally {
					logger.debug("Out of the indexing try catch block");
				}
			}
		} finally {
			long finished = System.currentTimeMillis();
			logger.info(String.format("Statistics: store %d", finished
					- started));
		}
		return false;
	}

	private void createIndexIfMissing(String year) throws Exception {
		if (!indexes.contains(year)) {
			String indexName = indexPrefix + "." + year;

			IndicesExistsResponse exists = esClient.admin().indices()
					.prepareExists(indexName).execute().actionGet();

			if (!exists.isExists()) {
				createIndex(year);
			}

			this.indexes.add(year);
		}

	}

	private boolean exists(String indexName, String messageId,
			IndexedObjectType type) {
		long started = System.currentTimeMillis();
		try {
			GetResponse response = esClient
					.prepareGet(indexName, type.getValue(), messageId)
					.setFields("messageId").execute().actionGet();
			return response.isExists();
		} finally {
			long finished = System.currentTimeMillis();
			logger.debug(String.format("Statistics: exists %d", finished
					- started));

		}
	}

	public MessageSearchBuilder getNewBuilder() {
		return MessageSearchBuilder.newBuilder(esClient
				.prepareSearch(indexPrefix));
	}

	public MessageSearchBuilder getNewBuilder(MessageSearchBuilder oldBuilder) {
		MessageSearchBuilder newBuilder = MessageSearchBuilder
				.newBuilder(esClient.prepareSearch(indexPrefix));
		return newBuilder.buildFrom(oldBuilder);
	}

	public SearchResponse search(MessageSearchBuilder builder) {
		try {
			SearchRequestBuilder search = builder.build();
			logger.info(search.toString());
			SearchResponse response = search.execute().actionGet();
			return response;
		} catch (SearchPhaseExecutionException e) {
			logger.warn(e);
			return null;
		}
	}

	public long getMatchCount(String search) {
		try {
			SearchRequestBuilder builder = esClient.prepareSearch(indexPrefix);
			QueryBuilder qb = QueryBuilders.queryString(search)
					.defaultOperator(Operator.AND);

			builder.setQuery(qb);
			builder.setSearchType(SearchType.COUNT);
			SearchResponse response = builder.execute().actionGet();

			return response.getHits().getTotalHits();
		} catch (SearchPhaseExecutionException e) {
			logger.warn(e);
			return -1;
		}
	}

	public SearchResponse getMatchCount(MessageSearchBuilder builder) {
		try {
			SearchResponse response = builder.build()
					.setSearchType(SearchType.COUNT).execute().actionGet();

			return response;

		} catch (SearchPhaseExecutionException e) {
			logger.warn(e);
			return null;
		}
	}

	private static String readJsonDefinition(String fileName) throws Exception {
		return readFileInClasspath("/estemplate/" + fileName);
	}

	/**
	 * Read a file in classpath and return its content
	 * 
	 * @param url
	 *            File URL Example : /es/twitter/_settings.json
	 * @return File content or null if file doesn't exist
	 * @throws Exception
	 */
	public static String readFileInClasspath(String url) throws Exception {
		StringBuffer bufferJSON = new StringBuffer();

		try {
			InputStream ips = IndexService.class.getResourceAsStream(url);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;

			while ((line = br.readLine()) != null) {
				bufferJSON.append(line);
			}
			br.close();
		} catch (Exception e) {
			return null;
		}

		return bufferJSON.toString();
	}

	public void refresh() throws Exception {
		RefreshResponse refresh = esClient.admin().indices()
				.prepareRefresh(indexPrefix).execute().actionGet();
		if (refresh.getFailedShards() > 0) {
			throw new Exception("Refresh failed");
		}
	}

}
