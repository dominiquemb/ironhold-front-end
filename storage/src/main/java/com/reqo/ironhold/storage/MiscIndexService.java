package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.metadata.IMAPBatchMeta;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class MiscIndexService extends AbstractIndexService {
    public static final String PARTITION = "1";
    private static Logger logger = Logger.getLogger(MiscIndexService.class);
    public static final String SUFFIX = "misc";
    private static Map<IndexedObjectType, String> mappings;

    static {
        mappings = new HashMap<>();
        mappings.put(IndexedObjectType.PST_FILE_META, "miscPSTFileMetaIndexMapping.json");
        mappings.put(IndexedObjectType.IMAP_BATCH_META, "miscIMAPBatchMetaIndexMapping.json");
        mappings.put(IndexedObjectType.LOGIN_USER, "metaDataLoginUserIndexMapping.json");
    }

    public MiscIndexService(IndexClient client) {
        super(SUFFIX, client, "miscIndexSettings.json", mappings);
    }


    public void store(String indexPrefix, PSTFileMeta meta) throws Exception {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, PARTITION);

        createIndexIfMissing(indexPrefix, PARTITION);

        client.store(
                indexName,
                IndexedObjectType.PST_FILE_META,
                meta.serialize());

    }

    public void store(String indexPrefix, IMAPBatchMeta meta) throws Exception {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, PARTITION);

        createIndexIfMissing(indexPrefix, PARTITION);

        client.store(
                indexName,
                IndexedObjectType.IMAP_BATCH_META,
                meta.serialize());

    }

    public boolean exists(String indexPrefix, PSTFileMeta meta) throws Exception {
        createIndexIfMissing(indexPrefix, PARTITION);

        String alias = getIndexAlias(indexPrefix);
        SearchRequestBuilder searchRequestBuilder = client.getSearchRequestBuilder(alias);
        SearchResponse response = searchRequestBuilder
                .setTypes(IndexedObjectType.PST_FILE_META.getValue())
                .setSearchType(SearchType.COUNT)
                .setFilter(FilterBuilders
                        .andFilter(
                                FilterBuilders.termFilter("md5", meta.getMd5()),
                                FilterBuilders.termFilter("size", meta.getSize())
                        )).execute().actionGet();

        return response.getHits().getTotalHits() > 0;

    }

    public List<PSTFileMeta> getPSTFileMeta(String indexPrefix, int from, int limit) throws IOException, ExecutionException, InterruptedException {
        String alias = getIndexAlias(indexPrefix);
        SearchResponse response = client.getByType(alias, IndexedObjectType.PST_FILE_META, from, limit);
        List<PSTFileMeta> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            PSTFileMeta pstFileMeta = new PSTFileMeta();

            result.add(pstFileMeta.deserialize(hit.getSourceAsString()));
        }

        return result;
    }

    public List<IMAPBatchMeta> getIMAPBatchMeta(String indexPrefix, int from, int limit) throws IOException, ExecutionException, InterruptedException {
        String alias = getIndexAlias(indexPrefix);
        SearchResponse response = client.getByType(alias, IndexedObjectType.IMAP_BATCH_META, from, limit);
        List<IMAPBatchMeta> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            IMAPBatchMeta imapBatchMeta = new IMAPBatchMeta();

            result.add(imapBatchMeta.deserialize(hit.getSourceAsString()));
        }

        return result;
    }


    public void store(String indexPrefix, LoginUser loginUser) throws Exception {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, PARTITION);

        createIndexIfMissing(indexPrefix, PARTITION);

        client.store(
                indexName,
                IndexedObjectType.LOGIN_USER,
                loginUser.getUsername(),
                loginUser.serialize());
    }


    public List<LoginUser> getLoginUsers(String indexPrefix, int start, int limit) throws IOException, ExecutionException, InterruptedException {
        String alias = getIndexAlias(indexPrefix);
        SearchResponse response = client.getByType(alias, IndexedObjectType.LOGIN_USER, start, limit);
        List<LoginUser> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            LoginUser loginUser = new LoginUser();

            result.add(loginUser.deserialize(hit.getSourceAsString()));
        }

        return result;
    }

    public LoginUser authenticate(String indexPrefix, String username, String password) throws Exception {
        String alias = getIndexAlias(indexPrefix);
        GetResponse response = client.getById(alias, IndexedObjectType.LOGIN_USER, username);

        if (!response.exists()) {
            return null;
        }

        LoginUser storedUser = new LoginUser();
        storedUser = storedUser.deserialize(response.getSourceAsString());

        if (storedUser.getHashedPassword().equals(CheckSumHelper.getCheckSum(password.getBytes())) &&
                storedUser.hasRole(RoleEnum.CAN_LOGIN)) {
            storedUser.setLastLogin(new Date());
            store(indexPrefix, storedUser);
            return storedUser;
        }

        return null;

    }


}
