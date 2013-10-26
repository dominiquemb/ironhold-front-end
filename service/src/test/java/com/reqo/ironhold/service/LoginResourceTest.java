package com.reqo.ironhold.service;

import com.reqo.ironhold.storage.LocalMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.message.Recipient;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import com.reqo.ironhold.storage.security.IKeyStoreService;
import com.reqo.ironhold.storage.security.LocalKeyStoreService;
import com.reqo.ironhold.uploadclient.ImportFileClient;
import com.reqo.ironhold.uploadclient.LoginClient;
import com.reqo.ironhold.utils.MD5CheckSum;
import fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean;
import fr.pilato.spring.elasticsearch.ElasticsearchNodeFactoryBean;
import org.apache.commons.io.FileUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class LoginResourceTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private LocalMimeMailMessageStorageService mimeMailMessageStorageService;

    @Autowired
    private MetaDataIndexService metaDataIndexService;

    @Autowired
    private MiscIndexService miscIndexService;

    @Autowired
    private MessageIndexService messageIndexService;

    @Autowired
    private IndexClient indexClient;

    @Autowired
    private IKeyStoreService keyStoreService;

    @Autowired
    private ElasticsearchClientFactoryBean esClient;

    @Autowired
    private ElasticsearchNodeFactoryBean esNode;


    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder clientFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder serviceFolder = new TemporaryFolder();

    private HttpServer server;
    private String baseUrl;
    private LoginUser sampleUser;
    private String password = "secret";
    private String username = "testUser";
    private String clientKey = "test";

    @Before
    public void setUp() throws Exception {


        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore().getParentFile());
        deleteIfExists(new File("/tmp/es/data"));
        FileUtils.forceMkdir(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore());

        // start the server
        baseUrl = "http://localhost:1111/myapp/";
        server = Main.startServer(baseUrl, serviceFolder.getRoot());

        sampleUser = new LoginUser();
        sampleUser.setUsername(username);
        sampleUser.setHashedPassword(CheckSumHelper.getCheckSum(password.getBytes()));
        sampleUser.setRolesBitMask(RoleEnum.CAN_LOGIN.getValue());
        sampleUser.setMainRecipient(new Recipient("Sample User", "sample@user.net"));

        miscIndexService.store(clientKey, sampleUser);

        indexClient.refresh(clientKey + "." + MiscIndexService.SUFFIX);

        Assert.assertNotNull(miscIndexService.authenticate(clientKey, username, password));

    }

    private void deleteIfExists(File file) throws IOException {
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        deleteIfExists(((LocalKeyStoreService) keyStoreService).getKeyStore());
        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore().getParentFile());
        esClient.getObject().admin().indices().prepareDelete("_all").execute().actionGet();
        this.metaDataIndexService.clearCache();
        this.miscIndexService.clearCache();
        this.messageIndexService.clearCache();
    }


    @Test
    public void testBadLogin() throws Exception {
        LoginClient client = new LoginClient(baseUrl);
        boolean result = client.login("client", "username", "password");
        Assert.assertFalse(result);
    }

    @Test
    public void testGoodLogin() throws Exception {

        LoginClient client = new LoginClient(baseUrl);
        boolean result = client.login(clientKey, username, password);
        Assert.assertTrue(result);
    }

}
