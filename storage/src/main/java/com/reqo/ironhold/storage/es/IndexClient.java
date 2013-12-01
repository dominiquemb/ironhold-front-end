package com.reqo.ironhold.storage.es;

import com.reqo.ironhold.web.domain.Recipient;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
    private String restHost;
    private int restPort;
    private boolean joinCluster;

    public IndexClient(String nodeName) {
        this(nodeName, true);
    }

    public IndexClient(String nodeName, boolean joinCluster) {
        try {

            this.joinCluster = joinCluster;
            Properties prop = new Properties();
            prop.load(IndexClient.class
                    .getResourceAsStream("elasticsearch.properties"));

            esHosts = prop.getProperty("hosts").split(",");
            esPort = Integer.parseInt(prop.getProperty("port"));

            restHost = prop.getProperty("rest.host");
            restPort = Integer.parseInt(prop.getProperty("rest.port"));

            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            this.nodeName = nodeName + "@" + hostname;
            reconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public IndexClient(Client esClient) {
        this.esClient = esClient;
    }

    private void reconnect() {
        if (esClient != null) {
            esClient.close();
        }
        if (joinCluster) {

            Settings settings = ImmutableSettings.settingsBuilder()
                    .put("discovery.zen.ping.multicast.enabled", false)
                    .put("discovery.zen.ping.unicast.enabled", true)
                    .put("discovery.zen.ping.unicast.hosts", StringUtils.join(esHosts, ","))
                    .put("node.name", this.nodeName)
                    .build();

            Node node = NodeBuilder.nodeBuilder().client(true).settings(settings).node().start();
            esClient = node.client();

        } else {

            esClient = new TransportClient()
                    .addTransportAddress(new InetSocketTransportAddress(restHost, esPort));
        }


    }

    /**
     * ITEM OPERATIONS *
     */

    public void store(String indexName, IndexedObjectType type, String id, String object) {
        try {
            IndexResponse result =
                    esClient.prepareIndex(indexName, type.getValue(), id)
                            .setSource(object)
                            .execute()
                            .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    public void store(String indexName, IndexedObjectType type, String object) {
        try {
            IndexResponse result =
                    esClient.prepareIndex(indexName, type.getValue())
                            .setSource(object)
                            .execute()
                            .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public DeleteResponse delete(String indexName, IndexedObjectType type, String id) {
        try {
            return esClient.prepareDelete(indexName, type.getValue(),
                    id).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean itemExists(String indexName, IndexedObjectType type, String id) {
        GetResponse response = null;
        try {
            response = esClient
                    .prepareGet(indexName, type.getValue(), id)
                    .setFields("_id").execute().get();
            return response.isExists();

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public GetResponse getById(String indexName, IndexedObjectType type, String id) {
        try {
            return esClient.prepareGet(indexName, type.getValue(), id).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public SearchResponse getByField(String indexName, IndexedObjectType type, Set<Pair<String, String>> criteria, String sortField, SortOrder order) {
        try {

            SearchRequestBuilder request = esClient.prepareSearch(indexName)
                    .setTypes(type.getValue())
                    .addField("_source");

            if (sortField != null) {
                request.addSort(sortField, order);
            }

            AndFilterBuilder fb = FilterBuilders.andFilter();
            for (Pair<String, String> criterion : criteria) {
                fb.add(FilterBuilders.termFilter(criterion.getKey(), criterion.getValue()));
            }

            request.setFilter(fb);

            logger.debug(request.toString());
            return request.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public SearchResponse getByField(String indexName, IndexedObjectType type, Set<Pair<String, String>> criteria) {
        return getByField(indexName, type, criteria, null, SortOrder.DESC);
    }

    public SearchResponse getByType(String indexName, IndexedObjectType type, int start, int limit) {
        try {
            SearchRequestBuilder request = esClient.prepareSearch(indexName)
                    .setTypes(type.getValue())
                    .addField("_source").setFrom(start).setSize(limit);
            logger.debug(request.toString());
            return request.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * INDEX OPERATIONS *
     */


    public boolean indexExists(String indexName) {
        IndicesExistsResponse exists = null;
        try {
            exists = esClient.admin().indices()
                    .prepareExists(indexName).execute().get();

            return exists.isExists();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void createIndex(String indexName, String alias, String indexSettingsFile) {
        try {
            if (indexName == null || alias == null) {
                throw new IllegalArgumentException("Index or alias cannot be null");
            }
            String indexSettingsFileContents = readJsonDefinition(indexSettingsFile);

            CreateIndexResponse response1 = esClient.admin().indices()
                    .prepareCreate(indexName).setSettings(indexSettingsFileContents).execute()
                    .get();
            if (!response1.isAcknowledged()) {
                throw new RuntimeException("ES Request did not get acknowledged: "
                        + response1.toString());
            }

            if (!indexName.equals(alias)) {
                IndicesAliasesResponse response3 = esClient.admin().indices()
                        .prepareAliases().addAlias(indexName, alias).execute()
                        .get();
                if (!response3.isAcknowledged()) {
                    throw new RuntimeException("ES Request did not get acknowledged: "
                            + response3.toString());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void addTypeMapping(String indexName, IndexedObjectType type, String mappingsFile) {
        try {

            String mappingFileContents = readJsonDefinition(mappingsFile);
            logger.info("Applying mapping for " + indexName + " of type " + type.getValue());
            PutMappingResponse response2 = null;
            response2 = esClient.admin().indices()
                    .preparePutMapping(indexName)
                    .setType(type.getValue())
                    .setSource(mappingFileContents).execute().get();
            if (!response2.isAcknowledged()) {
                throw new RuntimeException("ES Request did not get acknowledged: "
                        + response2.toString());
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }


    public void refresh(String indexPrefix) {
        try {
            RefreshResponse refresh = esClient.admin().indices()
                    .prepareRefresh(indexPrefix).execute().get();
            if (refresh.getFailedShards() > 0) {
                throw new RuntimeException("Refresh failed");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * SEARCH METHODS *
     */

    public SearchRequestBuilder getSearchRequestBuilder(String alias) {
        SearchRequestBuilder search = esClient.prepareSearch(alias);
        return search;
    }

    public SearchRequestBuilder getSearchRequestBuilder(String alias, LoginUser loginUser) {
        SearchRequestBuilder search = esClient.prepareSearch(alias);
        applyFilters(search, loginUser);
        return search;
    }


    private void applyFilters(SearchRequestBuilder search, LoginUser loginUser) {
        if (loginUser.hasRole(RoleEnum.CAN_SEARCH) || loginUser.hasRole(RoleEnum.CAN_SEARCH_ALL)) {
            if (!loginUser.hasRole(RoleEnum.CAN_SEARCH_ALL)) {
                OrFilterBuilder filterBuilders = FilterBuilders.orFilter();
                addLoginFilter(filterBuilders, loginUser.getMainRecipient().getAddress());
                if (loginUser.getRecipients() != null) {
                    for (Recipient recipient : loginUser.getRecipients()) {
                        addLoginFilter(filterBuilders, recipient.getAddress());
                    }
                }
                if (loginUser.getSources() != null) {
                    for (String source : loginUser.getSources()) {
                        addSourceFilter(filterBuilders, source);
                    }
                }
                search.setFilter(filterBuilders);
            }
        } else {
            throw new SecurityException("This user does not have search role");
        }

    }

    public static void addSourceFilter(OrFilterBuilder filterBuilders, String source) {
        filterBuilders.add(FilterBuilders.orFilter(FilterBuilders.inFilter("sources", source)));
    }

    public static void addLoginFilter(OrFilterBuilder filterBuilders, String address) {
        filterBuilders.add(FilterBuilders.orFilter(FilterBuilders.inFilter("sender.address", address)));
        filterBuilders.add(FilterBuilders.orFilter(FilterBuilders.inFilter("to.address", address)));
        filterBuilders.add(FilterBuilders.orFilter(FilterBuilders.inFilter("cc.address", address)));
        filterBuilders.add(FilterBuilders.orFilter(FilterBuilders.inFilter("bcc.address", address)));
    }


    /**
     * Helper methods
     */
    private static String readJsonDefinition(String fileName) {
        return readFileInClasspath("/estemplate/" + fileName);
    }

    /**
     * Read a file in classpath and return its content
     *
     * @param url File URL Example : /es/twitter/_settings.json
     * @return File content or null if file doesn't exist
     * @
     */
    public static String readFileInClasspath(String url) {
        try {
            StringBuffer bufferJSON = new StringBuffer();


            InputStream ips = IndexClient.class.getResourceAsStream(url);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String line;

            while ((line = br.readLine()) != null) {
                bufferJSON.append(line);
            }
            br.close();
            return bufferJSON.toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Client getClient() {
        return esClient;
    }

    public long getCount(String alias, IndexedObjectType objectType) throws ExecutionException, InterruptedException {
        CountResponse response = esClient.prepareCount(alias).setTypes(objectType.getValue()).execute().get();
        return response.getCount();
    }
}
