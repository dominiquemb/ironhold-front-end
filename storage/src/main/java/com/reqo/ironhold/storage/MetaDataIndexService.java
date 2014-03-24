package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.interfaces.IMetaDataIndexService;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.source.BloombergSource;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.message.source.MessageSource;
import com.reqo.ironhold.storage.model.message.source.PSTMessageSource;
import com.reqo.ironhold.storage.model.search.IndexFailure;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.web.domain.AuditActionEnum;
import com.reqo.ironhold.web.domain.AuditLogMessage;
import com.reqo.ironhold.web.domain.LoginUser;
import com.reqo.ironhold.web.domain.RoleEnum;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class MetaDataIndexService extends AbstractIndexService implements IMetaDataIndexService {
    private static Logger logger = Logger.getLogger(MetaDataIndexService.class);
    public static final String SUFFIX = "meta";
    private static Map<IndexedObjectType, String> mappings;

    static {
        mappings = new HashMap<>();
        mappings.put(IndexedObjectType.BLOOMBERG_SOURCE, "metaDataBloombergSourceIndexMapping.json");
        mappings.put(IndexedObjectType.PST_MESSAGE_SOURCE, "metaDataPSTMessageSourceIndexMapping.json");
        mappings.put(IndexedObjectType.IMAP_MESSAGE_SOURCE, "metaDataIMAPMessageSourceIndexMapping.json");
        mappings.put(IndexedObjectType.LOG_MESSAGE, "metaDataLogMessageIndexMapping.json");
        mappings.put(IndexedObjectType.AUDIT_LOG_MESSAGE, "metaDataAuditLogMessageIndexMapping.json");
        mappings.put(IndexedObjectType.INDEX_FAILURE, "metaDataIndexFailureIndexMapping.json");
    }

    public MetaDataIndexService(IndexClient client) {
        super(SUFFIX, client, "metaDataIndexSettings.json", mappings);
    }


    public void store(String indexPrefix, IndexFailure failure) {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, failure.getPartition());

        createIndexIfMissing(indexPrefix, failure.getPartition());

        client.store(
                indexName,
                IndexedObjectType.INDEX_FAILURE,
                failure.getMessageId(),
                failure.serialize());
    }

    public void store(String indexPrefix, MessageSource source) {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, source.getPartition());

        createIndexIfMissing(indexPrefix, source.getPartition());

        if (source instanceof IMAPMessageSource) {
            client.store(
                    indexName,
                    IndexedObjectType.IMAP_MESSAGE_SOURCE,
                    source.getId(),
                    ((IMAPMessageSource) source).serialize());
        } else if (source instanceof PSTMessageSource) {
            client.store(
                    indexName,
                    IndexedObjectType.PST_MESSAGE_SOURCE,
                    source.getId(),
                    ((PSTMessageSource) source).serialize());
        } else if (source instanceof BloombergSource) {
            client.store(
                    indexName,
                    IndexedObjectType.BLOOMBERG_SOURCE,
                    source.getId(),
                    ((BloombergSource) source).serialize());
        } else {
            throw new NotImplementedException();
        }


    }

    public List<MessageSource> getSources(String indexPrefix, String messageId) {
        String alias = getIndexAlias(indexPrefix);
        Set<Pair<String, String>> criteria = new HashSet<>();
        criteria.add(new ImmutablePair("messageId", messageId));
        SearchResponse response = client.getByField(alias, IndexedObjectType.PST_MESSAGE_SOURCE, criteria);
        List<MessageSource> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            PSTMessageSource source = new PSTMessageSource();

            result.add(source.deserialize(hit.getSourceAsString()));
        }

        SearchResponse response2 = client.getByField(alias, IndexedObjectType.IMAP_MESSAGE_SOURCE, criteria);
        for (SearchHit hit : response2.getHits()) {
            IMAPMessageSource source = new IMAPMessageSource();

            result.add(source.deserialize(hit.getSourceAsString()));
        }

        SearchResponse response3 = client.getByField(alias, IndexedObjectType.BLOOMBERG_SOURCE, criteria);
        for (SearchHit hit : response3.getHits()) {
            BloombergSource source = new BloombergSource();

            result.add(source.deserialize(hit.getSourceAsString()));
        }

        return result;
    }


    public void store(String indexPrefix, LogMessage logMessage) {

        createIndexIfMissing(indexPrefix, logMessage.getPartition());
        String indexName = getIndexName(getIndexAlias(indexPrefix), logMessage.getPartition());

        client.store(
                indexName,
                IndexedObjectType.LOG_MESSAGE,
                logMessage.serialize());

    }


    public void store(String indexPrefix, AuditLogMessage auditLogMessage) {

        if (!auditLogMessage.getLoginUser().hasRole(RoleEnum.SUPER_USER)) {
            createIndexIfMissing(indexPrefix, auditLogMessage.getPartition());
            String indexName = getIndexName(getIndexAlias(indexPrefix), auditLogMessage.getPartition());

            client.store(
                    indexName,
                    IndexedObjectType.AUDIT_LOG_MESSAGE,
                    auditLogMessage.serialize());
        }


    }


    public List<AuditLogMessage> getAuditLogMessages(String indexPrefix, String messageId) {
        String alias = getIndexAlias(indexPrefix);
        Set<Pair<String, String>> criteria = new HashSet<>();
        criteria.add(new ImmutablePair("messageId", messageId));

        SearchResponse response = client.getByField(alias, IndexedObjectType.AUDIT_LOG_MESSAGE, criteria, "timestamp", SortOrder.DESC);
        List<AuditLogMessage> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            AuditLogMessage auditLogMessage = new AuditLogMessage();

            result.add(auditLogMessage.deserialize(hit.getSourceAsString()));
        }

        return result;
    }


    public List<AuditLogMessage> getAuditLogMessages(String indexPrefix, LoginUser loginUser, AuditActionEnum action) {
        String alias = getIndexAlias(indexPrefix);
        Set<Pair<String, String>> criteria = new HashSet<>();
        criteria.add(new ImmutablePair("username", loginUser.getUsername()));
        criteria.add(new ImmutablePair("action", action.toString()));
        SearchResponse response = client.getByField(alias, IndexedObjectType.AUDIT_LOG_MESSAGE, criteria, "timestamp", SortOrder.DESC);
        List<AuditLogMessage> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            AuditLogMessage auditLogMessage = new AuditLogMessage();
            auditLogMessage = auditLogMessage.deserialize(hit.getSourceAsString());
            if (auditLogMessage.getAction() == action) {
                result.add(auditLogMessage);
            }
        }

        return result;
    }

    public List<LogMessage> getLogMessages(String indexPrefix, String messageId) {
        String alias = getIndexAlias(indexPrefix);
        Set<Pair<String, String>> criteria = new HashSet<>();
        criteria.add(new ImmutablePair("messageId", messageId));

        SearchResponse response = client.getByField(alias, IndexedObjectType.LOG_MESSAGE, criteria, "timestamp", SortOrder.DESC);
        List<LogMessage> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            LogMessage logMessage = new LogMessage();

            result.add(logMessage.deserialize(hit.getSourceAsString()));
        }

        return result;
    }

    public List<IndexFailure> getIndexFailures(String indexPrefix, String criteria, int limit) {
        String alias = getIndexAlias(indexPrefix);
        SearchResponse response = client.getByType(alias, criteria, IndexedObjectType.INDEX_FAILURE, 0, limit);
        List<IndexFailure> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            IndexFailure indexFailure = new IndexFailure();

            result.add(indexFailure.deserialize(hit.getSourceAsString()));
        }

        return result;
    }


}
