package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.storage.model.metadata.BloombergMeta;
import com.reqo.ironhold.storage.model.metadata.IMAPBatchMeta;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import com.reqo.ironhold.web.domain.LoginChannelEnum;
import com.reqo.ironhold.web.domain.LoginUser;
import com.reqo.ironhold.web.domain.RoleEnum;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.*;


public class MiscIndexService extends AbstractIndexService implements IMiscIndexService {
    public static final String PARTITION = null;
    private static Logger logger = Logger.getLogger(MiscIndexService.class);
    public static final String SUFFIX = "misc";
    private static Map<IndexedObjectType, String> mappings;

    static {
        mappings = new HashMap<>();
        mappings.put(IndexedObjectType.PST_FILE_META, "miscPSTFileMetaIndexMapping.json");
        mappings.put(IndexedObjectType.IMAP_BATCH_META, "miscIMAPBatchMetaIndexMapping.json");
        mappings.put(IndexedObjectType.BLOOMBERG_META, "miscBloombergMetaIndexMapping.json");
        mappings.put(IndexedObjectType.LOGIN_USER, "metaDataLoginUserIndexMapping.json");
    }

    public MiscIndexService(IndexClient client) {
        super(SUFFIX, client, "miscIndexSettings.json", mappings);
    }


    public void store(String indexPrefix, PSTFileMeta meta) {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, PARTITION);

        createIndexIfMissing(indexPrefix, PARTITION);

        client.store(
                indexName,
                IndexedObjectType.PST_FILE_META,
                meta.getId(),
                meta.serialize());

    }

    public void store(String indexPrefix, IMAPBatchMeta meta) {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, PARTITION);

        createIndexIfMissing(indexPrefix, PARTITION);

        client.store(
                indexName,
                IndexedObjectType.IMAP_BATCH_META,
                meta.serialize());

    }

    public void store(String indexPrefix, BloombergMeta meta) {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, PARTITION);

        createIndexIfMissing(indexPrefix, PARTITION);

        client.store(
                indexName,
                IndexedObjectType.BLOOMBERG_META,
                meta.serialize());

    }


    public PSTFileMeta findExisting(String indexPrefix, PSTFileMeta meta) {
        createIndexIfMissing(indexPrefix, PARTITION);

        String alias = getIndexAlias(indexPrefix);
        SearchRequestBuilder searchRequestBuilder = client.getSearchRequestBuilder(alias);
        SearchResponse response = searchRequestBuilder
                .setTypes(IndexedObjectType.PST_FILE_META.getValue())
                .setFilter(FilterBuilders
                        .andFilter(
                                FilterBuilders.termFilter("md5", meta.getMd5()),
                                FilterBuilders.termFilter("size", meta.getSize())
                        )).execute().actionGet();

        if (response.getHits().getHits().length > 0) {
            PSTFileMeta pstFileMeta = new PSTFileMeta();

            return pstFileMeta.deserialize(response.getHits().getAt(0).getSourceAsString());
        }
        return null;
    }

    public PSTFileMeta getPSTFileMeta(String indexPrefix, String id) {
        createIndexIfMissing(indexPrefix, PARTITION);

        String alias = getIndexAlias(indexPrefix);
        GetResponse response = client.getById(alias, IndexedObjectType.PST_FILE_META, id);
        if (response.isExists()) {
            PSTFileMeta pstFileMeta = new PSTFileMeta();

            return pstFileMeta.deserialize(response.getSourceAsString());
        }

        return null;
    }

    public List<PSTFileMeta> getPSTFileMetas(String indexPrefix, String criteria, int from, int limit) {
        createIndexIfMissing(indexPrefix, PARTITION);

        String alias = getIndexAlias(indexPrefix);
        SearchResponse response = client.getByType(alias, criteria, IndexedObjectType.PST_FILE_META, from, limit);
        List<PSTFileMeta> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            PSTFileMeta pstFileMeta = new PSTFileMeta();

            result.add(pstFileMeta.deserialize(hit.getSourceAsString()));
        }

        return result;
    }

    public List<IMAPBatchMeta> getIMAPBatchMeta(String indexPrefix, String criteria, int from, int limit) {
        String alias = getIndexAlias(indexPrefix);
        SearchResponse response = client.getByType(alias, criteria, IndexedObjectType.IMAP_BATCH_META, from, limit);
        List<IMAPBatchMeta> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            IMAPBatchMeta imapBatchMeta = new IMAPBatchMeta();

            result.add(imapBatchMeta.deserialize(hit.getSourceAsString()));
        }

        return result;
    }


    public void store(String indexPrefix, LoginUser loginUser) {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, PARTITION);

        createIndexIfMissing(indexPrefix, PARTITION);

        client.store(
                indexName,
                IndexedObjectType.LOGIN_USER,
                loginUser.getId(),
                loginUser.serialize());
    }


    public List<LoginUser> getLoginUsers(String indexPrefix, String criteria, int start, int limit) {
        String alias = getIndexAlias(indexPrefix);
        SearchResponse response = client.getByType(alias, criteria, IndexedObjectType.LOGIN_USER, start, limit);
        List<LoginUser> result = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            LoginUser loginUser = new LoginUser();

            result.add(loginUser.deserialize(hit.getSourceAsString()));
        }

        return result;
    }

    public LoginUser authenticate(String indexPrefix, String username, String password, LoginChannelEnum channel, String loginContext) {
        LoginUser storedUser = usernameExists(indexPrefix, username.toLowerCase());
        if (storedUser == null) {
            return null;
        }

        if (storedUser.getHashedPassword().equals(CheckSumHelper.getCheckSum(password.getBytes())) &&
                storedUser.hasRole(RoleEnum.CAN_LOGIN)) {
            storedUser.setLastLogin(new Date());
            storedUser.setLastLoginChannel(channel.name());
            storedUser.setLastLoginContext(loginContext);
            store(indexPrefix, storedUser);
            return storedUser;
        }

        return null;

    }

    public LoginUser getLoginUser(String indexPrefix, String username) {
        return usernameExists(indexPrefix, username.toLowerCase());
    }


    public long getLoginUserCount(String indexPrefix) {
        String alias = getIndexAlias(indexPrefix);
        return client.getCount(alias, IndexedObjectType.LOGIN_USER);
    }

    public LoginUser usernameExists(String indexPrefix, String username) {
        String alias = getIndexAlias(indexPrefix);
        Set<Pair<String, String>> criteria = new HashSet<>();
        Pair<String, String> pair = new ImmutablePair("username", username.toLowerCase());
        criteria.add(pair);
        SearchResponse response = client.getByField(alias, IndexedObjectType.LOGIN_USER, criteria);
        if (response.getHits().getTotalHits() != 1) {
            return null;
        } else {
            LoginUser storedUser = new LoginUser();
            return storedUser.deserialize(response.getHits().getAt(0).getSourceAsString());
        }
    }
}
