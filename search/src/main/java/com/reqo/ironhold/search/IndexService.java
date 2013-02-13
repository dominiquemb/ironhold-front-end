package com.reqo.ironhold.search;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
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
	private TransportClient esClient;
	private final String indexName;
	private String[] esHosts;
	private int esPort;

	public IndexService(String indexName) throws Exception {
		this.indexName = indexName;

		Properties prop = new Properties();
		prop.load(IndexService.class
				.getResourceAsStream("elasticsearch.properties"));

		esHosts = prop.getProperty("hosts").split(",");
		esPort = Integer.parseInt(prop.getProperty("port"));

		reconnect();

	}

	private void reconnect() throws Exception {
		if (esClient != null) {
			esClient.close();
		}
		esClient = new TransportClient();

		for (String esHost : esHosts) {
			esClient.addTransportAddress(new InetSocketTransportAddress(esHost,
					esPort));
		}

		IndicesExistsResponse exists = esClient.admin().indices()
				.prepareExists(indexName).execute().actionGet();

		if (!exists.isExists()) {
			createIndex();
		}
	}

	private void createIndex() throws Exception {
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

	}

	public void dropIndex() throws Exception {
		DeleteIndexResponse response = esClient.admin().indices()
				.prepareDelete(indexName).execute().actionGet();
		if (!response.acknowledged()) {
			throw new Exception("ES Request did not get acknowledged: "
					+ response.toString());
		}
	}

	public boolean store(IndexedMailMessage message) throws Exception {
		if (exists(message.getMessageId(), message.getType())) {
			esClient.prepareDelete(indexName, message.getType().getValue(),
					message.getMessageId()).execute().actionGet();
			logger.info(String.format("Deleting document with id [%s]",
					message.getMessageId()));

		}

		long started = System.currentTimeMillis();
		int retry = 1;
		boolean success = false;
		while (!success && retry <= MAX_RETRY_COUNT) {
			try {

				logger.info("Trying to index " + message.getMessageId());

				esClient.prepareIndex(indexName, message.getType().getValue(),
						message.getMessageId())
						.setSource(IndexedMailMessage.toJSON(message))
						.execute().actionGet();

				logger.info("Returned from ES");
				success = true;
				return true;
			} catch (NoNodeAvailableException e) {
				logger.warn("Recieved no node available exception, sleep for "
						+ RETRY_SLEEP + " (Attempt " + retry + " out of "
						+ MAX_RETRY_COUNT + ")");
				success = false;
				retry++;

				try {
					Thread.sleep(RETRY_SLEEP);
				} catch (InterruptedException ignore) {
				}
				reconnect();
			} finally {
				logger.info("Out of the indexing try catch block");
			}
		}
		long finished = System.currentTimeMillis();
		logger.info(String.format("Statistics: store %d", finished - started));

		return false;
	}

	private boolean exists(String messageId, IndexedObjectType type) {
		long started = System.currentTimeMillis();
		try {
			GetResponse response = esClient
					.prepareGet(indexName, type.getValue(), messageId)
					.setFields("messageId").execute().actionGet();
			return response.isExists();
		} finally {
			long finished = System.currentTimeMillis();
			logger.info(String.format("Statistics: exists %d", finished
					- started));

		}
	}

	public MessageSearchBuilder getNewBuilder() {
		return MessageSearchBuilder.newBuilder(esClient
				.prepareSearch(indexName));
	}

	public MessageSearchBuilder getNewBuilder(MessageSearchBuilder oldBuilder) {
		MessageSearchBuilder newBuilder = MessageSearchBuilder
				.newBuilder(esClient.prepareSearch(indexName));
		return newBuilder.buildFrom(oldBuilder);
	}

	public SearchResponse search(MessageSearchBuilder builder) {
		SearchRequestBuilder search = builder.build();
		logger.info(search.toString());
		SearchResponse response = search.execute().actionGet();
		return response;
	}

	public long getMatchCount(String search) {

		SearchRequestBuilder builder = esClient.prepareSearch(indexName);
		QueryBuilder qb = QueryBuilders.queryString(search).defaultOperator(Operator.AND);
		
		builder.setQuery(qb);
		builder.setSearchType(SearchType.COUNT);
		SearchResponse response = builder.execute().actionGet();

		return response.getHits().getTotalHits();
	}

	public SearchResponse getMatchCount(MessageSearchBuilder builder) {
		SearchResponse response = builder.build()
				.setSearchType(SearchType.COUNT).execute().actionGet();

		return response;
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

}
