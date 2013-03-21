package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.model.search.IndexedMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MessageIndexService {
    private static Logger logger = Logger.getLogger(MessageIndexService.class);
    private Set<String> indexes;
    private final IndexClient client;

    public MessageIndexService(IndexClient client) throws Exception {

        indexes = Collections.synchronizedSet(new HashSet<String>());
        this.client = client;
    }

    private void createMessageIndex(String indexPrefix, String year) throws Exception {
        String indexName = indexPrefix + "." + year;
        client.createIndex(indexName, indexPrefix, "messageIndexSettings.json");
        client.addTypeMapping(indexName, IndexedObjectType.MIME_MESSAGE, "messageIndexMappings.json");
    }

    public boolean store(String indexPrefix, IndexedMailMessage message) throws Exception {
        String indexName = indexPrefix + "." + message.getYear();

        createMessageIndexIfMissing(indexPrefix, message.getYear());

        logger.debug("Trying to index " + message.getMessageId());

        IndexResponse response = client.store(
                indexName,
                IndexedObjectType.MIME_MESSAGE,
                message.getMessageId(),
                IndexedMailMessage.toJSON(message));

        logger.debug("Returned from ES");
        return true;
    }


    public MessageSearchBuilder getNewBuilder(String alias) {
        return MessageSearchBuilder.newBuilder(client.getSearchRequestBuilder(alias));
    }

    public MessageSearchBuilder getNewBuilder(String alias, MessageSearchBuilder oldBuilder) {
        MessageSearchBuilder newBuilder = MessageSearchBuilder
                .newBuilder(client.getSearchRequestBuilder(alias));
        return newBuilder.buildFrom(oldBuilder);
    }

    private void createMessageIndexIfMissing(String indexPrefix, String year) throws Exception {
        if (!indexes.contains(year)) {
            String indexName = indexPrefix + "." + year;

            if (!client.indexExists(indexName)) {
                createMessageIndex(indexPrefix, year);
            }

            this.indexes.add(year);
        }

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

    public long getMatchCount(String indexPrefix, String search) {
        try {
            SearchRequestBuilder builder = client.getSearchRequestBuilder(indexPrefix);
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


}
