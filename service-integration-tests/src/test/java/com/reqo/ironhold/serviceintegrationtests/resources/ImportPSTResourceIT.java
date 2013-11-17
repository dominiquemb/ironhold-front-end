package com.reqo.ironhold.serviceintegrationtests.resources;

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
import fr.pilato.spring.elasticsearch.ElasticsearchTransportClientFactoryBean;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.UUID;

/**
 * User: ilya
 * Date: 11/3/13
 * Time: 9:48 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:testContextClient.xml")
public class ImportPSTResourceIT extends TestCase {
    private static Logger logger = Logger.getLogger(ImportPSTResourceIT.class);

    private URI siteBase;
    private HtmlUnitDriver driver;
    private static final String URL = System.getProperty("integration-test.url");

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

    private LoginUser sampleUser;
    private String samplePassword = "secret";
    private String sampleUsername = "testUser";
    private String sampleClientKey = "test";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        siteBase = new URI(URL + "importpst/session");
        driver = new HtmlUnitDriver();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                driver.close();
            }
        });

        sampleUser = new LoginUser();
        sampleUser.setUsername(sampleUsername);
        sampleUser.setHashedPassword(CheckSumHelper.getCheckSum(samplePassword.getBytes()));
        sampleUser.setRolesBitMask(RoleEnum.CAN_LOGIN.getValue());
        sampleUser.setMainRecipient(new Recipient("Sample User", "sample@user.net"));

        miscIndexService.store(sampleClientKey, sampleUser);

        indexClient.refresh(sampleClientKey + "." + MiscIndexService.SUFFIX);

        Thread.sleep(1000);
        LoginUser storedUser = miscIndexService.authenticate(sampleClientKey, sampleUsername, samplePassword, LoginChannelEnum.WEB_APP, "192.168.1.1");
        org.junit.Assert.assertNotNull(storedUser);
        org.junit.Assert.assertEquals(LoginChannelEnum.WEB_APP.name(), storedUser.getLastLoginChannel());
        org.junit.Assert.assertEquals("192.168.1.1", storedUser.getLastLoginContext());

    }


    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGetSession() {
        driver.get(siteBase.toString());
        String source = driver.getPageSource();
        logger.info(source);
        Assert.assertTrue(source.contains("j_spring_security"));

        WebElement username = driver.findElementByName("j_username");
        WebElement password  = driver.findElementByName("j_password");
        WebElement submit  = driver.findElementByName("submit");

        username.sendKeys(sampleClientKey + "/" + sampleUsername);
        password.sendKeys(samplePassword);
        submit.click();

        source = driver.getPageSource();
        logger.info(source);

        Assert.assertFalse(source.contains("HTTP"));

        String sessionId = source;
        Assert.assertNotNull(UUID.fromString(sessionId));

    }
}