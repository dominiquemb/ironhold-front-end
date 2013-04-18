package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: ilya
 * Date: 3/21/13
 * Time: 8:00 AM
 */
public abstract class AbstractIndexService {
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


    protected void createIndexIfMissing(String indexPrefix, String partition) throws Exception {
        if (!indexes.contains(partition)) {
            String indexAlias = getIndexAlias(indexPrefix);
            String indexName = getIndexName(indexAlias, partition);

            if (!client.indexExists(indexName)) {
                client.createIndex(indexName, indexAlias, indexSettingsFile);
                for (IndexedObjectType type : mappings.keySet()) {
                    client.addTypeMapping(indexName, type, mappings.get(type));
                }

            }

            this.indexes.add(partition);
        }

    }

    public void clearCache() {
        indexes.clear();
    }

}
