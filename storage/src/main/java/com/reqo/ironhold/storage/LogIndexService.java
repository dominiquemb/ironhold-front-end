package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.log.LogMessage;
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

public class LogIndexService extends AbstractIndexService {
    private static Logger logger = Logger.getLogger(LogIndexService.class);
    public static final String SUFFIX = "logs";
    private static Map<IndexedObjectType, String> mappings;

    static {
        mappings = new HashMap<>();
        mappings.put(IndexedObjectType.LOG_MESSAGE, "logIndexMappings.json");
    }

    public LogIndexService(IndexClient client) {
        super(SUFFIX, client, "logIndexSettings.json", mappings);
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


}
