package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.model.search.IndexedMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.storage.model.user.LoginUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
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
        store(indexPrefix, message, true);
    }

    public void store(String indexPrefix, IndexedMailMessage message, boolean checkIfExists) throws Exception {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, message.getPartition());

        createIndexIfMissing(indexPrefix, message.getPartition());

        if (!checkIfExists || !client.itemExists(indexName, IndexedObjectType.MIME_MESSAGE, message.getMessageId())) {
            client.store(
                    indexName,
                    IndexedObjectType.MIME_MESSAGE,
                    message.getMessageId(),
                    message.serialize());
        }
    }

    public IndexedMailMessage getById(String indexPrefix, String partition, String messageId) throws Exception {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, partition);

        createIndexIfMissing(indexPrefix, partition);


        GetResponse response = client.getById(indexName, IndexedObjectType.MIME_MESSAGE, messageId);
        if (response.exists()) {
            IndexedMailMessage indexedMailMessage = new IndexedMailMessage();
            indexedMailMessage = indexedMailMessage.deserialize(response.getSourceAsString());

            return indexedMailMessage;
        }
        return null;
    }

    public boolean exists(String indexPrefix, String partition, String messageId) throws Exception {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, partition);

        createIndexIfMissing(indexPrefix, partition);

        return client.itemExists(indexName, IndexedObjectType.MIME_MESSAGE, messageId);
    }

    public MessageSearchBuilder getNewBuilder(String alias, LoginUser loginUser) throws Exception {
        if (alias == null || StringUtils.isEmpty(alias)) {
            throw new Exception("Alias cannot be blank");
        }
        return MessageSearchBuilder.newBuilder(client.getSearchRequestBuilder(alias, loginUser));
    }

    public MessageSearchBuilder getNewBuilder(String alias, MessageSearchBuilder oldBuilder, LoginUser loginUser) throws Exception {
        MessageSearchBuilder newBuilder = MessageSearchBuilder
                .newBuilder(client.getSearchRequestBuilder(alias, loginUser));
        return newBuilder.buildFrom(oldBuilder);
    }

    public SearchResponse search(MessageSearchBuilder builder, LoginUser loginUser) {
        try {
            SearchRequestBuilder search = builder.build(loginUser);
            logger.debug(search.toString());
            SearchResponse response = search.execute().actionGet();
            return response;
        } catch (Exception e) {
            logger.warn(e);
            return null;
        }
    }

    public long getMatchCount(String indexPrefix, String search, LoginUser loginUser) throws Exception {
        try {
            SearchRequestBuilder builder = client.getSearchRequestBuilder(indexPrefix, loginUser);
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

    public SearchResponse getMatchCount(MessageSearchBuilder builder, LoginUser loginUser) {
        try {
            SearchResponse response = builder.build(loginUser)
                    .setSearchType(SearchType.COUNT).execute().actionGet();

            return response;

        } catch (Exception e) {
            logger.warn(e);
            return null;
        }
    }

    public long getTotalMessageCount(String indexPrefix, LoginUser loginUser) throws Exception {
        return client.getTotalMessageCount(indexPrefix, loginUser);
    }
}
