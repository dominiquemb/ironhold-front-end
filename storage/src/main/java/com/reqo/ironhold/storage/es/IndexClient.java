package com.reqo.ironhold.storage.es;

import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * User: ilya
 * Date: 3/20/13
 * Time: 7:49 AM
 */
public class IndexClient {
    private static Logger logger = Logger.getLogger(IndexClient.class);

    private Client esClient;
    private String[] esHosts;
    private int esPort;
    private String nodeName;

    public IndexClient(String nodeName) throws Exception {
        Properties prop = new Properties();
        prop.load(IndexClient.class
                .getResourceAsStream("elasticsearch.properties"));

        esHosts = prop.getProperty("hosts").split(",");
        esPort = Integer.parseInt(prop.getProperty("port"));


        String hostname = java.net.InetAddress.getLocalHost().getHostName();
        this.nodeName = nodeName + "@" + hostname;
        reconnect();

    }

    public IndexClient(Client esClient) {
        this.esClient = esClient;
    }


    private void reconnect() throws Exception {
        if (esClient != null) {
            esClient.close();
        }

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("discovery.zen.ping.multicast.enabled", false)
                .put("discovery.zen.ping.unicast.enabled", true)
                .put("discovery.zen.ping.unicast.hosts", StringUtils.join(esHosts, ","))
                .put("node.name", this.nodeName)
                .build();

        Node node = NodeBuilder.nodeBuilder().client(true).settings(settings).node().start();

        esClient = node.client();


    }

    /**
     * ITEM OPERATIONS *
     */

    public IndexResponse store(String indexName, IndexedObjectType type, String id, String object) {
        IndexResponse result =
                esClient.prepareIndex(indexName, type.getValue(), id)
                        .setSource(object)
                        .execute()
                        .actionGet();

        return result;
    }

    public IndexResponse store(String indexName, IndexedObjectType type, String object) {
        IndexResponse result =
                esClient.prepareIndex(indexName, type.getValue())
                        .setSource(object)
                        .execute()
                        .actionGet();

        return result;
    }

    public DeleteResponse delete(String indexName, IndexedObjectType type, String id) {
        return esClient.prepareDelete(indexName, type.getValue(),
                id).execute().actionGet();

    }

    public boolean itemExists(String indexName, IndexedObjectType type, String id) {
        GetResponse response = esClient
                .prepareGet(indexName, type.getValue(), id)
                .setFields("_id").execute().actionGet();
        return response.isExists();
    }

    public GetResponse getById(String indexName, IndexedObjectType type, String id) {
        return esClient.prepareGet(indexName, type.getValue(), id).execute().actionGet();
    }

    public SearchResponse getByField(String indexName, IndexedObjectType type, String field, String value) {
        SearchRequestBuilder request = esClient.prepareSearch(indexName)
                .setTypes(type.getValue())
                .addField("_source")
                .setFilter(FilterBuilders.termFilter(field, value));
        logger.debug(request.toString());
        return request

                .execute().actionGet();
    }


    public SearchResponse getByType(String indexName, IndexedObjectType type, int start, int limit) {
        SearchRequestBuilder request = esClient.prepareSearch(indexName)
                .setTypes(type.getValue())
                .addField("_source").setFrom(start).setSize(limit);
        logger.debug(request.toString());
        return request

                .execute().actionGet();
    }

    /**
     * INDEX OPERATIONS *
     */


    public boolean indexExists(String indexName) {
        IndicesExistsResponse exists = esClient.admin().indices()
                .prepareExists(indexName).execute().actionGet();

        return exists.isExists();
    }

    public void createIndex(String indexName, String alias, String indexSettingsFile) throws Exception {
        String indexSettingsFileContents = readJsonDefinition(indexSettingsFile);

        CreateIndexResponse response1 = esClient.admin().indices()
                .prepareCreate(indexName).setSettings(indexSettingsFileContents).execute()
                .actionGet();
        if (!response1.acknowledged()) {
            throw new Exception("ES Request did not get acknowledged: "
                    + response1.toString());
        }


        IndicesAliasesResponse response3 = esClient.admin().indices()
                .prepareAliases().addAlias(indexName, alias).execute()
                .actionGet();
        if (!response3.acknowledged()) {
            throw new Exception("ES Request did not get acknowledged: "
                    + response3.toString());
        }

    }

    public void addTypeMapping(String indexName, IndexedObjectType type, String mappingsFile) throws Exception {
        String mappingFileContents = readJsonDefinition(mappingsFile);
        PutMappingResponse response2 = esClient.admin().indices()
                .preparePutMapping(indexName)
                .setType(type.getValue())
                .setSource(mappingFileContents).execute().actionGet();
        if (!response2.acknowledged()) {
            throw new Exception("ES Request did not get acknowledged: "
                    + response2.toString());
        }
    }


    public void refresh(String indexPrefix) throws Exception {
        RefreshResponse refresh = esClient.admin().indices()
                .prepareRefresh(indexPrefix).execute().actionGet();
        if (refresh.getFailedShards() > 0) {
            throw new Exception("Refresh failed");
        }
    }

    /**
     * SEARCH METHODS *
     */

    public SearchRequestBuilder getSearchRequestBuilder(String alias) {
        return esClient.prepareSearch(alias);
    }


    public long getTotalMessageCount(String alias) {
        return esClient.prepareSearch(alias).setSearchType(SearchType.COUNT).execute().actionGet().getHits().getTotalHits();
    }

    /**
     * Helper methods
     */

    private static String readJsonDefinition(String fileName) throws Exception {
        return readFileInClasspath("/estemplate/" + fileName);
    }

    /**
     * Read a file in classpath and return its content
     *
     * @param url File URL Example : /es/twitter/_settings.json
     * @return File content or null if file doesn't exist
     * @throws Exception
     */
    public static String readFileInClasspath(String url) throws Exception {
        StringBuffer bufferJSON = new StringBuffer();

        try {
            InputStream ips = IndexClient.class.getResourceAsStream(url);
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