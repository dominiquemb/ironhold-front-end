package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.message.source.MessageSource;
import com.reqo.ironhold.storage.model.message.source.PSTMessageSource;
import com.reqo.ironhold.storage.model.search.IndexFailure;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageMetaDataIndexService extends AbstractIndexService {
    private static Logger logger = Logger.getLogger(MessageMetaDataIndexService.class);
    public static final String SUFFIX = "meta";
    private static Map<IndexedObjectType, String> mappings;

    static {
        mappings = new HashMap<>();
        mappings.put(IndexedObjectType.PST_MESSAGE_SOURCE, "metaDataPSTMessageSourceIndexMapping.json");
        mappings.put(IndexedObjectType.IMAP_MESSAGE_SOURCE, "metaDataIMAPMessageSourceIndexMapping.json");
        mappings.put(IndexedObjectType.LOG_MESSAGE, "metaDataLogMessageIndexMapping.json");
        mappings.put(IndexedObjectType.INDEX_FAILURE, "metaDataIndexFailureIndexMapping.json");
    }

    public MessageMetaDataIndexService(IndexClient client) {
        super(SUFFIX, client, "metaDataIndexSettings.json", mappings);
    }


    public void store(String indexPrefix, IndexFailure failure) throws Exception {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, failure.getPartition());

        createIndexIfMissing(indexPrefix, failure.getPartition());

        client.store(
                indexName,
                IndexedObjectType.INDEX_FAILURE,
                failure.getMessageId(),
                failure.serialize());
    }

    public void store(String indexPrefix, MessageSource source) throws Exception {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, source.getPartition());

        createIndexIfMissing(indexPrefix, source.getPartition());

        if (source instanceof IMAPMessageSource) {
            client.store(
                    indexName,
                    IndexedObjectType.IMAP_MESSAGE_SOURCE,
                    ((IMAPMessageSource) source).serialize());
        } else {
            client.store(
                    indexName,
                    IndexedObjectType.PST_MESSAGE_SOURCE,
                    ((PSTMessageSource) source).serialize());
        }

    }

    public List<MessageSource> getSources(String indexPrefix, String messageId) throws IOException {
        String alias = getIndexAlias(indexPrefix);
        SearchResponse response = client.getByField(alias, IndexedObjectType.PST_MESSAGE_SOURCE, "messageId", messageId);
        List<MessageSource> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            PSTMessageSource source = new PSTMessageSource();

            result.add(source.deserialize(hit.getSourceAsString()));
        }

        SearchResponse response2 = client.getByField(alias, IndexedObjectType.IMAP_MESSAGE_SOURCE, "messageId", messageId);
        for (SearchHit hit : response2.getHits()) {
            IMAPMessageSource source = new IMAPMessageSource();

            result.add(source.deserialize(hit.getSourceAsString()));
        }

        return result;
    }


    public IndexResponse store(String indexPrefix, LogMessage logMessage) throws Exception {

        createIndexIfMissing(indexPrefix, logMessage.getPartition());
        String indexName = getIndexName(getIndexAlias(indexPrefix), logMessage.getPartition());

        return client.store(
                indexName,
                IndexedObjectType.LOG_MESSAGE,
                logMessage.serialize());

    }

    public List<LogMessage> getLogMessages(String indexPrefix, String messageId) throws IOException {
        String alias = getIndexAlias(indexPrefix);
        SearchResponse response = client.getByField(alias, IndexedObjectType.LOG_MESSAGE, "messageId", messageId);
        List<LogMessage> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            LogMessage logMessage = new LogMessage();

            result.add(logMessage.deserialize(hit.getSourceAsString()));
        }

        return result;
    }

    public List<IndexFailure> getIndexFailures(String indexPrefix, int limit) throws Exception {
        String alias = getIndexAlias(indexPrefix);
        SearchResponse response = client.getByType(alias, IndexedObjectType.INDEX_FAILURE, 0, limit);
        List<IndexFailure> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            IndexFailure indexFailure = new IndexFailure();

            result.add(indexFailure.deserialize(hit.getSourceAsString()));
        }

        return result;
    }

}
