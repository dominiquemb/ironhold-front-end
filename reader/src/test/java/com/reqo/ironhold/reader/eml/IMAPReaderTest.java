package com.reqo.ironhold.reader.eml;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.reqo.ironhold.storage.LocalMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.security.IKeyStoreService;
import com.reqo.ironhold.storage.security.LocalKeyStoreService;
import fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean;
import fr.pilato.spring.elasticsearch.ElasticsearchNodeFactoryBean;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.Properties;

/**
 * User: ilya
 * Date: 6/11/13
 * Time: 9:06 AM
 */
@ContextConfiguration(locations = "classpath:IMAPReaderTest_context.xml")
public class IMAPReaderTest extends AbstractJUnit4SpringContextTests {
    private static final String TEST_CLIENT = "test";

    @Autowired
    private LocalMimeMailMessageStorageService mimeMailMessageStorageService;

    @Autowired
    private MetaDataIndexService metaDataIndexService;

    @Autowired
    private MiscIndexService miscIndexService;

    @Autowired
    private MessageIndexService messageIndexService;

    @Autowired
    private IMAPReader imapReader;

    @Autowired
    private IndexClient indexClient;

    @Autowired
    private IKeyStoreService keyStoreService;

    @Autowired
    private ElasticsearchClientFactoryBean esClient;

    @Autowired
    private ElasticsearchNodeFactoryBean esNode;

    private GreenMail greenMail;
    private GreenMailUser user;

    @Before
    public void setUp() throws Exception {


        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore().getParentFile());
        deleteIfExists(new File("/tmp/es/data"));
        FileUtils.forceMkdir(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore());
        FileUtils.forceMkdir(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getArchiveStore());

        greenMail = new GreenMail(new ServerSetup[]{new ServerSetup(3000, "localhost", "imap"), new ServerSetup(3001, "localhost", "smtp")});
        greenMail.start();
        user = greenMail.setUser("to@localhost.com", "login-id", "password");

        imapReader.setHostname("localhost");
        imapReader.setPort(3000);
        imapReader.setUsername("login-id");
        imapReader.setPassword("password");
        imapReader.setProtocol("imap");
        imapReader.setClient(TEST_CLIENT);
        imapReader.setBatchSize(500);
        imapReader.setExpunge(true);
        imapReader.setTimeout(10000);
        // Calling processMail Function to read from IMAP Account
        imapReader.initiateConnection();
    }

    private void deleteIfExists(File file) throws IOException {
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }

    @After
    public void tearDown() throws Exception {
        deleteIfExists(((LocalKeyStoreService) keyStoreService).getKeyStore());
        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore().getParentFile());
        esClient.getObject().admin().indices().prepareDelete("_all").execute().actionGet();
        this.metaDataIndexService.clearCache();
        this.miscIndexService.clearCache();
        this.messageIndexService.clearCache();
    }

    @Test
    @Ignore
    public void testSmallBatch() throws UserException, FileNotFoundException, MessagingException, InterruptedException {
        {
            File file = FileUtils.toFile(IMAPReaderTest.class
                    .getResource("/testSmallBatch.eml"));
            InputStream is = new FileInputStream(file);

            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imap");
            props.setProperty("mail.mime.base64.ignoreerrors", "true");
            props.setProperty("mail.imap.partialfetch", "false");
            props.setProperty("mail.imaps.partialfetch", "false");
            Session session = Session.getInstance(props, null);

            MimeMessage message = new MimeMessage(session, is);
            user.deliver(message);
        }

        {
            File file = FileUtils.toFile(IMAPReaderTest.class
                    .getResource("/testSmallBatch2.eml"));
            InputStream is = new FileInputStream(file);

            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imap");
            props.setProperty("mail.mime.base64.ignoreerrors", "true");
            props.setProperty("mail.imap.partialfetch", "false");
            props.setProperty("mail.imaps.partialfetch", "false");
            Session session = Session.getInstance(props, null);
            MimeMessage message = new MimeMessage(session, is);
            user.deliver(message);

        }

        Assert.assertEquals(2, greenMail.getReceivedMessages().length);
        int number = imapReader.processMail();
        Assert.assertEquals(2, number);
        Assert.assertEquals(0, greenMail.getReceivedMessages().length);

    }
}
