package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.alias.get.IndicesGetAliasesResponse;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * User: ilya
 * Date: 3/21/13
 * Time: 8:00 AM
 */
public abstract class AbstractIndexService {
    private static Logger logger = Logger.getLogger(AbstractIndexService.class);

    private final String suffix;
    private Set<String> indexes;

    protected final IndexClient client;
    private final String indexSettingsFile;
    private final Map<IndexedObjectType, String> mappings;

    public AbstractIndexService(String suffix, IndexClient client, String indexSettingsFile, Map<IndexedObjectType, String> mappings) {
        this.suffix = suffix;
        this.client = client;
        this.indexSettingsFile = indexSettingsFile;
        this.mappings = mappings;
        indexes = Collections.synchronizedSet(new HashSet<String>());
    }

    public AbstractIndexService(IndexClient client, String indexSettingsFile, Map<IndexedObjectType, String> mappings) {
        this(null, client, indexSettingsFile, mappings);
    }


    public String getIndexAlias(String indexPrefix) {
        if (suffix != null) {
            return indexPrefix + "." + suffix;
        } else {
            return indexPrefix;
        }
    }

    public String getIndexName(String alias, String partition) {
        return partition == null ? alias : alias + "." + partition;
    }


    protected synchronized void createIndexIfMissing(String indexPrefix, String partition) throws Exception {
        if (!indexes.contains(partition == null || partition.isEmpty() ? "none" : partition)) {
            String indexAlias = getIndexAlias(indexPrefix);
            String indexName = getIndexName(indexAlias, partition);

            if (!client.indexExists(indexName)) {
                client.createIndex(indexName, indexAlias, indexSettingsFile);
                Thread.sleep(1000);
                for (IndexedObjectType type : mappings.keySet()) {
                    client.addTypeMapping(indexName, type, mappings.get(type));
                }
                Thread.sleep(1000);

            } else {
                for (IndexedObjectType type : mappings.keySet()) {
                    client.addTypeMapping(indexName, type, mappings.get(type));
                }
                Thread.sleep(1000);
            }

            this.indexes.add(partition == null || partition.isEmpty() ? "none" : partition);
        }

    }

    public void clearCache() {
        indexes.clear();
    }

    public void deleteByField(String indexPrefix, String partition, IndexedObjectType type, String field, String value) throws ExecutionException, InterruptedException {
        String indexAlias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(indexAlias, partition);

        QueryBuilder qb = QueryBuilders.fieldQuery(field, value);
        client.getClient().prepareDeleteByQuery(indexName).setTypes(type.getValue()).setQuery(qb).execute().get();
    }


    public void forceRefreshMappings(String indexPrefix) throws Exception {
        IndicesGetAliasesResponse response = client.getClient().admin().indices().prepareGetAliases(indexPrefix).execute().get();
        Map<String, List<AliasMetaData>> aliases = response.getAliases();
        for (String indexName : aliases.keySet()) {
            for (IndexedObjectType type : mappings.keySet()) {
                client.addTypeMapping(indexName, type, mappings.get(type));
            }
        }
        Thread.sleep(1000);
    }
}
