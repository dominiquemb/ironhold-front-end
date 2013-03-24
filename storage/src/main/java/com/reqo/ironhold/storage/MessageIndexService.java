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

import java.util.HashMap;
import java.util.Map;

public class MessageIndexService extends AbstractIndexService {
    private static Logger logger = Logger.getLogger(MessageIndexService.class);

    private static Map<IndexedObjectType, String> mappings;

    static {
        mappings = new HashMap<>();
        mappings.put(IndexedObjectType.MIME_MESSAGE, "messageIndexMapping.json");
    }

    public MessageIndexService(IndexClient client) {
        super(client, "messageIndexSettings.json", mappings);
    }

    public void store(String indexPrefix, IndexedMailMessage message) throws Exception {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, message.getPartition());

        createIndexIfMissing(indexPrefix, message.getPartition());

        IndexResponse response = client.store(
                indexName,
                IndexedObjectType.MIME_MESSAGE,
                message.getMessageId(),
                message.serialize());


    }


    public MessageSearchBuilder getNewBuilder(String alias) {
        return MessageSearchBuilder.newBuilder(client.getSearchRequestBuilder(alias));
    }

    public MessageSearchBuilder getNewBuilder(String alias, MessageSearchBuilder oldBuilder) {
        MessageSearchBuilder newBuilder = MessageSearchBuilder
                .newBuilder(client.getSearchRequestBuilder(alias));
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


    public long getTotalMessageCount(String indexPrefix) {
        return client.getTotalMessageCount(indexPrefix);
    }
}
