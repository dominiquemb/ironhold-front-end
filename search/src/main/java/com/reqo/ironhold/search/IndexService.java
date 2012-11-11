package com.reqo.ironhold.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.reqo.ironhold.storage.model.MailMessage;
import static junit.framework.Assert.*;

public class IndexService {
	private static final int RETRY_SLEEP = 10000;
	private static final int MAX_RETRY_COUNT = 10;

	private static Logger logger = Logger.getLogger(IndexService.class);
	private Client esClient;
	private final String indexName;

	public IndexService(String indexName) {
		this.indexName = indexName;
		reconnect();

	}

	private void reconnect() {
		if (esClient != null) {
			esClient.close();
		}
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("client.transport.ping_timeout", "30s").build();
		esClient = new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", 9300));

	}

	public void createIndex() throws Exception {
		CreateIndexResponse response1 = esClient.admin().indices()
				.prepareCreate(indexName).execute().actionGet();
		if (!response1.acknowledged()) {
			throw new Exception("ES Request did not get acknowledged: "
					+ response1.toString());
		}
		String jsonDef = readJsonDefinition();
		PutMappingResponse response2 = esClient.admin().indices()
				.preparePutMapping(indexName).setType("message")
				.setSource(jsonDef).execute().actionGet();
		if (!response2.acknowledged()) {
			throw new Exception("ES Request did not get acknowledged: "
					+ response2.toString());
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

	public boolean store(MailMessage message) throws JsonGenerationException,
			JsonMappingException, ElasticSearchException, IOException {
		if (!exists(message.getMessageId())) {
			int retry = 1;
			boolean success = false;
			while (!success && retry <= MAX_RETRY_COUNT) {
				try {

					esClient.prepareIndex(indexName, "message",
							message.getMessageId())
							.setSource(MailMessage.toJSON(message)).execute()
							.actionGet();
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
				}
			}
		} else {
			logger.info(String.format("Skipped document with id [%s]",
					message.getMessageId()));
			return false;
		}

		return false;
	}

	private boolean exists(String messageId) {
		GetResponse response = esClient
				.prepareGet(indexName, "message", messageId).execute()
				.actionGet();
		return response.isExists();
	}

	public MessageSearchBuilder getNewBuilder() {
		return MessageSearchBuilder.newBuilder(esClient
				.prepareSearch(indexName));
	}

	public SearchResponse search(MessageSearchBuilder builder) {
		return builder.build().execute().actionGet();
	}

	public long getMatchCount(String search) {

		SearchRequestBuilder builder = esClient.prepareSearch(indexName);
		QueryBuilder qb = QueryBuilders.queryString(search);
		builder.setQuery(qb);
		builder.setSearchType(SearchType.COUNT);
		SearchResponse response = builder.execute().actionGet();

		return response.getHits().getTotalHits();
	}

	private static String readJsonDefinition() throws Exception {
		return readFileInClasspath("/estemplate/message.json");
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
