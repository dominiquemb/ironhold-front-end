package com.reqo.ironhold.storage;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchSetting;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.IMAPBatchMetaTestModel;
import com.reqo.ironhold.storage.model.LoginUserTestModel;
import com.reqo.ironhold.storage.model.PSTFileMetaTestModel;
import com.reqo.ironhold.storage.model.metadata.IMAPBatchMeta;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;
import com.reqo.ironhold.web.domain.LoginChannelEnum;
import com.reqo.ironhold.web.domain.LoginUser;
import com.reqo.ironhold.web.domain.RoleEnum;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RunWith(ElasticsearchRunner.class)
@SuppressWarnings("unchecked")
public class MiscIndexServiceTest {

    private static final String INDEX_PREFIX = "unittest";
    private MiscIndexService miscIndexService;

    @ElasticsearchNode(settings = {
            @ElasticsearchSetting(name = "script.disable_dynamic", value = "false")
    })
    private static Node node;

    @ElasticsearchClient
    private static Client client;

    private IndexClient indexClient;

    @Before
    public void setUp() throws Exception {
        indexClient = new IndexClient(client);
        miscIndexService = new MiscIndexService(indexClient);
    }

    @After
    public void tearDown() throws ExecutionException, InterruptedException {
        client.admin().indices().prepareDelete().execute().get();
    }


    @Test
    public void testStorePSTFileMeta() throws Exception {
        PSTFileMeta metaData = PSTFileMetaTestModel.generate();

        miscIndexService.store(INDEX_PREFIX, metaData);

        indexClient.refresh(INDEX_PREFIX + "." + MiscIndexService.SUFFIX);

        List<PSTFileMeta> pstFileMetaList = miscIndexService.getPSTFileMetas(INDEX_PREFIX, "*", 0, 10);

        Assert.assertEquals(1, pstFileMetaList.size());
        for (PSTFileMeta pstFileMeta : pstFileMetaList) {
            Assert.assertEquals(metaData.serialize(), pstFileMeta.serialize());
        }

        Assert.assertNotNull(miscIndexService.findExisting(INDEX_PREFIX, metaData));

        PSTFileMeta metaData2 = PSTFileMetaTestModel.generate();

        Assert.assertNull(miscIndexService.findExisting(INDEX_PREFIX, metaData2));

    }

    @Test
    public void testStoreIMAPBatchMeta() throws Exception {
        IMAPBatchMeta metaData = IMAPBatchMetaTestModel.generate();

        miscIndexService.store(INDEX_PREFIX, metaData);

        indexClient.refresh(INDEX_PREFIX + "." + MiscIndexService.SUFFIX);

        List<IMAPBatchMeta> imapBatchMetaList = miscIndexService.getIMAPBatchMeta(INDEX_PREFIX, "*", 0, 10);

        Assert.assertEquals(1, imapBatchMetaList.size());
        for (IMAPBatchMeta imapBatchMeta : imapBatchMetaList) {
            Assert.assertEquals(metaData.serialize(), imapBatchMeta.serialize());
        }
    }


    @Test
    public void testLoginUserStore() throws Exception {
        LoginUser loginUser = LoginUserTestModel.generate(RoleEnum.CAN_LOGIN);
        miscIndexService.store(INDEX_PREFIX, loginUser);

        indexClient.refresh(INDEX_PREFIX + "." + MiscIndexService.SUFFIX);

        List<LoginUser> loginUsers = miscIndexService.getLoginUsers(INDEX_PREFIX, "*", 0, 100);

        Assert.assertEquals(1, loginUsers.size());


        Assert.assertEquals(loginUser.serialize(), loginUsers.get(0).serialize());

    }


    @Test
    public void testAuthenticateNoSuchUser() throws Exception {
        LoginUser loginUser = LoginUserTestModel.generate(RoleEnum.CAN_LOGIN);
        miscIndexService.store(INDEX_PREFIX, loginUser);

        indexClient.refresh(INDEX_PREFIX + "." + MiscIndexService.SUFFIX);

        List<LoginUser> loginUsers = miscIndexService.getLoginUsers(INDEX_PREFIX, "*", 0, 100);

        Assert.assertEquals(1, loginUsers.size());


        Assert.assertEquals(loginUser.serialize(), loginUsers.get(0).serialize());

        Assert.assertNull(miscIndexService.authenticate(INDEX_PREFIX, loginUser.getUsername() + "!", loginUser.getUsername(), LoginChannelEnum.WEB_APP, "192.168.1.1"));

    }

    @Test
    public void testAuthenticateBadPassword() throws Exception {
        LoginUser loginUser = LoginUserTestModel.generate(RoleEnum.CAN_LOGIN);
        miscIndexService.store(INDEX_PREFIX, loginUser);

        indexClient.refresh(INDEX_PREFIX + "." + MiscIndexService.SUFFIX);

        List<LoginUser> loginUsers = miscIndexService.getLoginUsers(INDEX_PREFIX, "*", 0, 100);

        Assert.assertEquals(1, loginUsers.size());


        Assert.assertEquals(loginUser.serialize(), loginUsers.get(0).serialize());

        Assert.assertNull(miscIndexService.authenticate(INDEX_PREFIX, loginUser.getUsername(), loginUser.getUsername() + "!", LoginChannelEnum.WEB_APP, "192.168.1.1"));

    }

    @Test
    public void testAuthenticateCorrect() throws Exception {
        LoginUser loginUser = LoginUserTestModel.generate(RoleEnum.CAN_LOGIN);
        miscIndexService.store(INDEX_PREFIX, loginUser);

        indexClient.refresh(INDEX_PREFIX + "." + MiscIndexService.SUFFIX);

        List<LoginUser> loginUsers = miscIndexService.getLoginUsers(INDEX_PREFIX, "*", 0, 100);

        Assert.assertEquals(1, loginUsers.size());


        Assert.assertEquals(loginUser.serialize(), loginUsers.get(0).serialize());

        LoginUser authenticatedUser = miscIndexService.authenticate(INDEX_PREFIX, loginUser.getUsername(), loginUser.getUsername(), LoginChannelEnum.WEB_APP, "192.168.1.1");

        Assert.assertNotNull(authenticatedUser);

        Assert.assertEquals(loginUser.getUsername(), authenticatedUser.getUsername());
        Assert.assertNotSame(loginUser.getLastLogin(), authenticatedUser.getLastLogin());

    }

    @Test
    public void testAuthenticateCorrectSuperUser() throws Exception {
        LoginUser loginUser = LoginUserTestModel.generate(RoleEnum.SUPER_USER);
        miscIndexService.store(INDEX_PREFIX, loginUser);

        indexClient.refresh(INDEX_PREFIX + "." + MiscIndexService.SUFFIX);

        List<LoginUser> loginUsers = miscIndexService.getLoginUsers(INDEX_PREFIX, "*", 0, 100);

        Assert.assertEquals(1, loginUsers.size());


        Assert.assertEquals(loginUser.serialize(), loginUsers.get(0).serialize());

        LoginUser authenticatedUser = miscIndexService.authenticate(INDEX_PREFIX, loginUser.getUsername(), loginUser.getUsername(), LoginChannelEnum.WEB_APP, "192.168.1.1");

        Assert.assertNotNull(authenticatedUser);

        Assert.assertEquals(loginUser.getUsername(), authenticatedUser.getUsername());
        Assert.assertNotSame(loginUser.getLastLogin(), authenticatedUser.getLastLogin());

    }

    @Test
    public void testAuthenticateCorrectButNoRole() throws Exception {
        LoginUser loginUser = LoginUserTestModel.generate(RoleEnum.NONE);
        miscIndexService.store(INDEX_PREFIX, loginUser);

        indexClient.refresh(INDEX_PREFIX + "." + MiscIndexService.SUFFIX);

        List<LoginUser> loginUsers = miscIndexService.getLoginUsers(INDEX_PREFIX, "*", 0, 100);

        Assert.assertEquals(1, loginUsers.size());


        Assert.assertEquals(loginUser.serialize(), loginUsers.get(0).serialize());

        LoginUser authenticatedUser = miscIndexService.authenticate(INDEX_PREFIX, loginUser.getUsername(), loginUser.getUsername(), LoginChannelEnum.WEB_APP, "192.168.1.1");

        Assert.assertNull(authenticatedUser);

    }
}
