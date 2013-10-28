package com.reqo.ironhold.service.resources;

import com.reqo.ironhold.storage.LocalMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.message.Recipient;
import com.reqo.ironhold.storage.model.user.LoginChannelEnum;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import com.reqo.ironhold.storage.security.IKeyStoreService;
import com.reqo.ironhold.storage.security.LocalKeyStoreService;
import com.reqo.ironhold.uploadclient.LoginClient;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import fr.pilato.spring.elasticsearch.ElasticsearchNodeFactoryBean;
import fr.pilato.spring.elasticsearch.ElasticsearchTransportClientFactoryBean;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.ContextLoaderListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:testContextClient.xml")
public class LoginResourceTest extends JerseyTest {

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
    private ElasticsearchTransportClientFactoryBean esClient;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder clientFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder serviceFolder = new TemporaryFolder();

    private LoginUser sampleUser;
    private String password = "secret";
    private String username = "testUser";
    private String clientKey = "test";

    public LoginResourceTest() {
        super(new WebAppDescriptor.Builder("com.reqo.ironhold.service")
                .servletPath("service")
                .contextPath("webapi")
                .contextParam("contextConfigLocation", "classpath:/testContext.xml")
                .servletClass(SpringServlet.class)
                .initParam("javax.ws.rs.Application", "com.reqo.ironhold.service.JerseyApplication")
                .initParam("com.sun.jersey.api.json.POJOMappingFeature", "true")
                .contextListenerClass(ContextLoaderListener.class).build());
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore().getParentFile());
        deleteIfExists(new File("/tmp/es/data"));
        FileUtils.forceMkdir(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore());

        sampleUser = new LoginUser();
        sampleUser.setUsername(username);
        sampleUser.setHashedPassword(CheckSumHelper.getCheckSum(password.getBytes()));
        sampleUser.setRolesBitMask(RoleEnum.CAN_LOGIN.getValue());
        sampleUser.setMainRecipient(new Recipient("Sample User", "sample@user.net"));

        miscIndexService.store(clientKey, sampleUser);

        indexClient.refresh(clientKey + "." + MiscIndexService.SUFFIX);

        Thread.sleep(1000);
        LoginUser storedUser = miscIndexService.authenticate(clientKey, username, password, LoginChannelEnum.WEB_APP, "192.168.1.1");
        Assert.assertNotNull(storedUser);
        Assert.assertEquals(LoginChannelEnum.WEB_APP.name(), storedUser.getLastLoginChannel());
        Assert.assertEquals("192.168.1.1", storedUser.getLastLoginContext());

    }

    private void deleteIfExists(File file) throws IOException {
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        deleteIfExists(((LocalKeyStoreService) keyStoreService).getKeyStore());
        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore().getParentFile());
    }


    @Test
    public void testBadLogin() throws Exception {
        LoginClient client = new LoginClient(getBaseURI().toString() + "webapi/");
        boolean result = client.login("client", "username", "password", LoginChannelEnum.WEB_APP.name());
        Assert.assertFalse(result);
    }

    @Test
    public void testGoodLogin() throws Exception {

        LoginClient client = new LoginClient(getBaseURI().toString() + "webapi/");
        boolean result = client.login(clientKey, username, password, LoginChannelEnum.PST_UPLOAD.name());
        Assert.assertTrue(result);

        indexClient.refresh(clientKey + "." + MiscIndexService.SUFFIX);

        List<LoginUser> loginUsers = miscIndexService.getLoginUsers(clientKey, 0, 1);
        Assert.assertEquals(1, loginUsers.size());

        Assert.assertEquals(LoginChannelEnum.PST_UPLOAD.name(), loginUsers.get(0).getLastLoginChannel());
    }

}
