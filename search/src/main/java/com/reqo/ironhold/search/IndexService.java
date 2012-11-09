package com.reqo.ironhold.search;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elasticsearch.ElasticSearchException;
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
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.reqo.ironhold.storage.model.MailMessage;

public class IndexService {
	private static final int RETRY_SLEEP = 10000;
	private static final int MAX_RETRY_COUNT = 10;

	private static Logger logger = Logger.getLogger(IndexService.class);
	private Client esClient;
	private final String client;

	public IndexService(String client) {
		this.client = client;
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

	public boolean store(MailMessage message) throws JsonGenerationException,
			JsonMappingException, ElasticSearchException, IOException {
		if (!exists(message.getMessageId())) {
			int retry = 1;
			boolean success = false;
			while (!success && retry <= MAX_RETRY_COUNT) {
				try {

					esClient.prepareIndex(client, "message",
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
				.prepareGet(client, "message", messageId).execute().actionGet();
		return response.isExists();
	}

	public SearchHit[] search(String criteria) throws JsonParseException,
			JsonMappingException, IOException {

		return search(criteria, 0, 10);
	}

	public SearchHit searchAndFilterById(String id, String criteria, int from,
			int size) {

		SearchRequestBuilder builder = esClient.prepareSearch(client);
		QueryBuilder qb = QueryBuilders.filteredQuery(
				QueryBuilders.queryString(criteria),
				FilterBuilders.idsFilter("message").addIds(id));
		// QueryBuilder qb = QueryBuilders.fieldQuery("pstMessage.subject",
		// criteria);
		builder.setFrom(from).setSize(size);
		builder.setQuery(qb);
		// builder.setExplain(true);
		// builder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
		// builder.setHighlighterRequireFieldMatch(false);
		// builder.setHighlighterEncoder("styled");
		builder.addHighlightedField("pstMessage.subject", 0, 0);
		builder.addHighlightedField("pstMessage.body", 0, 0);
		builder.addHighlightedField("pstMessage.attachments.body");

		builder.addFields("pstMessage.subject", "pstMessage.body",
				"pstMessage.messageDeliveryTime", "pstMessage.messageSize",
				"pstMessage.sentRepresentingName",
				"pstMessage.sentRepresentingEmailAddress",
				"pstMessage.displayTo", "pstMessage.displayCc");
		builder.setHighlighterPreTags("<b>").setHighlighterPostTags("</b>");

		String q = builder.toString();
		SearchResponse response = builder.execute().actionGet();

		SearchHits hits = response.getHits();

		return hits.getHits()[0];
	}

	public SearchHit[] search(String criteria, int from, int size)
			throws JsonParseException, JsonMappingException, IOException {

		SearchRequestBuilder builder = esClient.prepareSearch(client);
		QueryBuilder qb = QueryBuilders.queryString(criteria);
		// QueryBuilder qb = QueryBuilders.fieldQuery("pstMessage.subject",
		// criteria);
		builder.setFrom(from).setSize(size);
		builder.setQuery(qb);
		// builder.setExplain(true);
		// builder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
		// builder.setHighlighterRequireFieldMatch(false);
		// builder.setHighlighterEncoder("styled");
		builder.addHighlightedField("pstMessage.subject", 0, 0);
		builder.addHighlightedField("pstMessage.body");
		builder.addHighlightedField("pstMessage.attachments.body");

		builder.addFields("pstMessage.subject", "pstMessage.body",
				"pstMessage.messageDeliveryTime", "pstMessage.messageSize",
				"pstMessage.sentRepresentingName",
				"pstMessage.sentRepresentingEmailAddress",
				"pstMessage.displayTo", "pstMessage.displayCc");
		builder.setHighlighterPreTags("<b>").setHighlighterPostTags("</b>");
		String q = builder.toString();
		SearchResponse response = builder.execute().actionGet();

		SearchHits hits = response.getHits();
		return hits.getHits();
	}

	public long getMatchCount(String search) {

		SearchRequestBuilder builder = esClient.prepareSearch(client);
		QueryBuilder qb = QueryBuilders.queryString(search);
		builder.setQuery(qb);
		builder.setSearchType(SearchType.COUNT);
		SearchResponse response = builder.execute().actionGet();

		return response.getHits().getTotalHits();
	}

}
