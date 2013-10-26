package com.reqo.ironhold.storage;

import com.pff.PSTMessage;
import com.reqo.ironhold.storage.model.MimeMailMessageTestModel;
import com.reqo.ironhold.storage.model.PSTMessageTestModel;
import com.reqo.ironhold.storage.model.exceptions.MessageExistsException;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.security.IKeyStoreService;
import com.reqo.ironhold.storage.security.LocalKeyStoreService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.HtmlEmail;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class MimeMailMessageStorageServiceTest {
    private PSTMessageTestModel testModel;
    private static final String TEST_CLIENT = "test";

    @Rule
    public TemporaryFolder dataFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder archiveFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder keyFolder = new TemporaryFolder();


    private LocalMimeMailMessageStorageService storageService;
    private Session session;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        testModel = new PSTMessageTestModel("/data.pst");

        String keyStorePath = keyFolder.getRoot().getAbsolutePath() + File.separator + "keystore";
        IKeyStoreService keyStoreService = new LocalKeyStoreService(new File(keyStorePath));
        storageService = new LocalMimeMailMessageStorageService(dataFolder.getRoot(), archiveFolder.getRoot(), keyStoreService);

        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.mime.base64.ignoreerrors", "true");
        props.setProperty("mail.imap.partialfetch", "false");
        props.setProperty("mail.imaps.partialfetch", "false");
        session = Session.getInstance(props, null);

    }

    @Test
    public void testExistsPositive() throws Exception {

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        Assert.assertTrue(storageService.exists(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId()));
    }

    @Test
    public void testGetPartitions() throws Exception {

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        Assert.assertTrue(storageService.exists(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId()));

        List<String> partitions = storageService.getPartitions(TEST_CLIENT);
        Assert.assertEquals(1, partitions.size());
        Assert.assertEquals(inputMessage.getPartition(), partitions.get(0));
    }

    @Test
    public void testGetSubPartitions() throws Exception {

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        Assert.assertTrue(storageService.exists(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId()));

        List<String> partitions = storageService.getPartitions(TEST_CLIENT);

        Assert.assertEquals(1, partitions.size());
        Assert.assertEquals(inputMessage.getPartition(), partitions.get(0));
        List<String> subPartitions = storageService.getSubPartitions(TEST_CLIENT, partitions.get(0));
        Assert.assertEquals(1, subPartitions.size());
        Assert.assertEquals(inputMessage.getSubPartition(), subPartitions.get(0));

    }

    @Test
    public void testGetList() throws Exception {

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        Assert.assertTrue(storageService.exists(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId()));

        List<String> partitions = storageService.getPartitions(TEST_CLIENT);

        Assert.assertEquals(1, partitions.size());
        Assert.assertEquals(inputMessage.getPartition(), partitions.get(0));
        List<String> subPartitions = storageService.getSubPartitions(TEST_CLIENT, partitions.get(0));
        Assert.assertEquals(1, subPartitions.size());
        Assert.assertEquals(inputMessage.getSubPartition(), subPartitions.get(0));

        List<String> files = storageService.getList(TEST_CLIENT, partitions.get(0), subPartitions.get(0));
        Assert.assertEquals(1, files.size());
        Assert.assertEquals(inputMessage.getMessageId(), files.get(0));

    }

    @Test
    public void testLargeMessage() throws Exception {

        HtmlEmail email = new HtmlEmail();
        email.addTo("ilya@erudites.com", "Ilya");
        email.setMsg("abc");

        email.setFrom("ilya@erudites.com", "Ilya");

        email.setSubject("subject");

        byte[] bytes = new byte[4096 * 100];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 2;
        }

        email.attach(new ByteArrayDataSource(bytes, "text/plain"), "fileName", "attachment");

        String hostname = java.net.InetAddress.getLocalHost().getHostName();
        email.setHostName(hostname);
        email.buildMimeMessage();

        MimeMessage sourceMimeMessage = email.getMimeMessage();

        MimeMessage mimeMessage = sourceMimeMessage;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mimeMessage.writeTo(baos);

        MimeMailMessage mimeMailMessage = new MimeMailMessage();
        mimeMailMessage.loadMimeMessageFromSource(baos.toString());

        storageService.store(TEST_CLIENT, mimeMailMessage.getPartition(), mimeMailMessage.getSubPartition(), mimeMailMessage.getMessageId(), mimeMailMessage.getRawContents(), mimeMailMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, mimeMailMessage);

        Assert.assertTrue(storageService.exists(TEST_CLIENT, mimeMailMessage.getPartition(), mimeMailMessage.getSubPartition(), mimeMailMessage
                .getMessageId()));
    }

    @Test
    public void testExistsNegative() throws Exception {


        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());


        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        Assert.assertFalse(storageService.exists(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), UUID.randomUUID()
                .toString()));
    }

    @Test
    public void testStore() throws Exception {

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);
    }


    @Test
    public void testMultipleStore() throws Exception {
        List<PSTMessage> pstMessages = testModel.generateOriginalPSTMessages();

        int superficialPartition = 0;
        for (PSTMessage pstMessage : pstMessages) {
            superficialPartition++;
            String partition = Integer.toString(superficialPartition % 3);
            MimeMessage mimeMessage = MimeMailMessage.getMimeMessage(pstMessage);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mimeMessage.writeTo(baos);
            String rawContents = baos.toString();
            rawContents = rawContents.replaceFirst("2008", partition);

            MimeMailMessage inputMessage = new MimeMailMessage();
            inputMessage.loadMimeMessageFromSource(rawContents);


            storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

            MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);
        }
    }

    @Test
    public void testStoreIfExists() throws Exception {


        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        try {
            storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), "test", inputMessage.getCheckSum(), true);

            Assert.assertTrue(false);
        } catch (MessageExistsException e) {

            Assert.assertTrue(e.getMessage().equals("Failed to store message [" + inputMessage.getMessageId() + "] for client [" + TEST_CLIENT + "] as it already exists"));
        }

    }

    @Test
    public void testStoreNotEncryptedIfExists() throws Exception {


        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), false);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        try {
            storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), "test", inputMessage.getCheckSum(), true);

            Assert.assertTrue(false);
        } catch (MessageExistsException e) {

            Assert.assertTrue(e.getMessage().equals("Failed to store message [" + inputMessage.getMessageId() + "] for client [" + TEST_CLIENT + "] as it already exists"));
        }

    }

    @Test
    public void testStoreNotEncryptedIfExists2() throws Exception {


        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        try {
            storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), "test", inputMessage.getCheckSum(), false);

            Assert.assertTrue(false);
        } catch (MessageExistsException e) {

            Assert.assertTrue(e.getMessage().equals("Failed to store message [" + inputMessage.getMessageId() + "] for client [" + TEST_CLIENT + "] as it already exists"));
        }

    }

    @Test
    public void testStoreNotEncryptedIfExists3() throws Exception {


        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), false);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        try {
            storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), "test", inputMessage.getCheckSum(), false);

            Assert.assertTrue(false);
        } catch (MessageExistsException e) {

            Assert.assertTrue(e.getMessage().equals("Failed to store message [" + inputMessage.getMessageId() + "] for client [" + TEST_CLIENT + "] as it already exists"));
        }

    }


    @Test
    public void testArchive() throws Exception {

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        storageService.archive(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId());

        MimeMailMessageTestModel.verifyArchiveStorage(TEST_CLIENT, storageService, inputMessage);

        Assert.assertFalse(storageService.exists(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId()));
    }

    @Test
    public void testIsEncryptedTrue() throws Exception {


        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        Assert.assertTrue(storageService.isEncrypted(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId()));

    }

    @Test
    public void testIsEncryptedFalse() throws Exception {


        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), false);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        Assert.assertFalse(storageService.isEncrypted(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId()));

    }

    @Test
    public void testListItems() throws Exception {

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());
        inputMessage.setMessageId("<!&!AAAAAAAAAAAYAAAAAAAAAB+oliEIEGlGkbY6RaPCH6/CgAAAEAAAAML1aQiLFvRMhxpcSxIzc28BAAAAAA==@whitestonelaw.com>");
        storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        Assert.assertTrue(storageService.isEncrypted(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId()));


        List<String> list = storageService.getList(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition());
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(storageService.normalizeMessageId(inputMessage.getMessageId()), list.get(0));


    }

    @Test
    public void testMessageWithNoDate() throws Exception {
        File file = FileUtils.toFile(MimeMailMessageStorageServiceTest.class
                .getResource("/testNoDate.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        Assert.assertEquals("unknown", mailMessage.getPartition());
        Assert.assertEquals("unknown", mailMessage.getSubPartition());

        storageService.store(TEST_CLIENT, mailMessage.getPartition(), mailMessage.getSubPartition(), mailMessage.getMessageId(), mailMessage.getRawContents(), mailMessage.getCheckSum(), true);

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, mailMessage);

    }


}
