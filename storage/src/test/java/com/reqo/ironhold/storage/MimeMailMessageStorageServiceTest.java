package com.reqo.ironhold.storage;

import com.mongodb.gridfs.GridFS;
import com.reqo.ironhold.storage.model.MimeMailMessageTestModel;
import com.reqo.ironhold.storage.model.PSTMessageTestModel;
import com.reqo.ironhold.storage.model.exceptions.MessageExistsException;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import junit.framework.Assert;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.HtmlEmail;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class MimeMailMessageStorageServiceTest {
    private PSTMessageTestModel testModel;
    private static final String TEST_CLIENT = "test";

    @Rule
    public TemporaryFolder parentFolder = new TemporaryFolder();


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        testModel = new PSTMessageTestModel("/data.pst");
    }

    @Test
    public void testExistsPositive() throws Exception {
        IMimeMailMessageStorageService storageService = new LocalMimeMailMessageStorageService(parentFolder.getRoot());

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getMessageId(), inputMessage.getRawContents());

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        Assert.assertTrue(storageService.exists(TEST_CLIENT, inputMessage.getMessageId()));
    }


    @Test
    public void testLargeMessage() throws Exception {
        IMimeMailMessageStorageService storageService = new LocalMimeMailMessageStorageService(parentFolder.getRoot());

        HtmlEmail email = new HtmlEmail();
        email.addTo("ilya@erudites.com", "Ilya");
        email.setMsg("abc");

        email.setFrom("ilya@erudites.com", "Ilya");

        email.setSubject("subject");

        byte[] bytes = new byte[(int) GridFS.MAX_CHUNKSIZE * 3];
        for (int i = 0; i < GridFS.MAX_CHUNKSIZE * 3; i++) {
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

        storageService.store(TEST_CLIENT, mimeMailMessage.getMessageId(), mimeMailMessage.getRawContents());

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, mimeMailMessage);

        Assert.assertTrue(storageService.exists(TEST_CLIENT, mimeMailMessage
                .getMessageId()));
    }

    @Test
    public void testExistsNegative() throws Exception {
        IMimeMailMessageStorageService storageService = new LocalMimeMailMessageStorageService(parentFolder.getRoot());

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getMessageId(), inputMessage.getRawContents());

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        Assert.assertFalse(storageService.exists(TEST_CLIENT, UUID.randomUUID()
                .toString()));
    }

    @Test
    public void testStore() throws Exception {
        IMimeMailMessageStorageService storageService = new LocalMimeMailMessageStorageService(parentFolder.getRoot());

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getMessageId(), inputMessage.getRawContents());

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);
    }

    @Test
    public void testStoreIfExists() throws Exception {

        IMimeMailMessageStorageService storageService = new LocalMimeMailMessageStorageService(parentFolder.getRoot());

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        storageService.store(TEST_CLIENT, inputMessage.getMessageId(), inputMessage.getRawContents());

        MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

        try {
            storageService.store(TEST_CLIENT, inputMessage.getMessageId(), "test");
            Assert.assertTrue(false);
        } catch (MessageExistsException e) {

            Assert.assertTrue(e.getMessage().equals("Failed to store message [" + inputMessage.getMessageId() + "] for client [" + TEST_CLIENT + "] as it already exists"));
        }

    }
       /*
    @Test
    public void testFindUnindexedIMAPMessages() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testMimeMessageWithHTML.eml"));
        InputStream is = new FileInputStream(file);

        List<String> orioginalLines = Files.readAllLines(
                Paths.get(file.toURI()), Charset.defaultCharset());
        StringBuilder original = new StringBuilder();
        for (String line : orioginalLines) {
            original.append(line + "\n");
        }

        MimeMailMessage mimeMailMessage = new MimeMailMessage();
        mimeMailMessage.loadMimeMessageFromSource(original.toString());
        mimeMailMessage.addSource(MessageSourceTestModel
                .generateIMAPMessageSource());

        storageService.store(mimeMailMessage);

        MimeMailMessageTestModel.verifyStorage(storageService, mimeMailMessage);

        List<MimeMailMessage> unindexedMessages = storageService
                .findUnindexedIMAPMessages(100);

        Assert.assertEquals(1, unindexedMessages.size());

        MimeMailMessage unindexedMessage = unindexedMessages.get(0);

        MimeMailMessageTestModel.verifyMimeMailMessage(mimeMailMessage,
                unindexedMessage);

    }

    @Test
    public void testAddPSTSource() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());
        inputMessage.addSource(MessageSourceTestModel
                .generatePSTMessageSource());

        storageService.store(inputMessage);

        MimeMailMessageTestModel.verifyStorage(storageService, inputMessage);

        PSTMessageSource source = MessageSourceTestModel
                .generatePSTMessageSource();
        storageService.addSource(inputMessage.getMessageId(), source);

        inputMessage.addSource(source);

        MimeMailMessageTestModel.verifyStorage(storageService, inputMessage);

    }

    @Test
    public void testAddIMAPSource() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testMimeMessageWithHTML.eml"));
        InputStream is = new FileInputStream(file);

        List<String> orioginalLines = Files.readAllLines(
                Paths.get(file.toURI()), Charset.defaultCharset());
        StringBuilder original = new StringBuilder();
        for (String line : orioginalLines) {
            original.append(line + "\n");
        }

        MimeMailMessage mimeMailMessage = new MimeMailMessage();
        mimeMailMessage.loadMimeMessageFromSource(original.toString());
        mimeMailMessage.addSource(MessageSourceTestModel
                .generateIMAPMessageSource());

        storageService.store(mimeMailMessage);

        MimeMailMessageTestModel.verifyStorage(storageService, mimeMailMessage);

        IMAPMessageSource source = MessageSourceTestModel
                .generateIMAPMessageSource();
        storageService.addSource(mimeMailMessage.getMessageId(), source);

        mimeMailMessage.addSource(source);

        MimeMailMessageTestModel.verifyStorage(storageService, mimeMailMessage);

    }


    @Test
    public void testIMAPMarkAsIndexed() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testMimeMessageWithHTML.eml"));
        InputStream is = new FileInputStream(file);

        List<String> orioginalLines = Files.readAllLines(
                Paths.get(file.toURI()), Charset.defaultCharset());
        StringBuilder original = new StringBuilder();
        for (String line : orioginalLines) {
            original.append(line + "\n");
        }

        MimeMailMessage inputMessage = new MimeMailMessage();
        inputMessage.loadMimeMessageFromSource(original.toString());
        inputMessage.addSource(MessageSourceTestModel
                .generateIMAPMessageSource());

        storageService.store(inputMessage);

        MimeMailMessageTestModel.verifyStorage(storageService, inputMessage);

        List<MimeMailMessage> unindexedMessages1 = storageService
                .findUnindexedIMAPMessages(100);
        Assert.assertEquals(1, unindexedMessages1.size());

        storageService.updateIndexStatus(inputMessage, IndexStatus.INDEXED);

        inputMessage.setIndexed(IndexStatus.INDEXED);

        MimeMailMessageTestModel.verifyStorage(storageService, inputMessage);

        List<MimeMailMessage> unindexedMessages2 = storageService
                .findUnindexedIMAPMessages(100);
        Assert.assertEquals(0, unindexedMessages2.size());

    }

    @Test
    public void testGetTotalMessageCount() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        List<PSTMessage> messages = testModel.generateOriginalPSTMessages();
        for (PSTMessage message : messages) {
            MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(message);
            inputMessage.addSource(MessageSourceTestModel
                    .generatePSTMessageSource());

            storageService.store(inputMessage);

            MimeMailMessageTestModel.verifyStorage(storageService, inputMessage);
        }

        Assert.assertEquals(messages.size(),
                storageService.getTotalMessageCount());
    }

    @Test
    public void testLog() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        LogMessage inputMessage = LogMessageTestModel.generate();

        storageService.store(inputMessage);

        List<LogMessage> storedMessages = storageService
                .getLogMessages(inputMessage.getMessageId());

        Assert.assertEquals(1, storedMessages.size());
        LogMessage storedMessage = storedMessages.get(0);
        Assert.assertEquals(LogMessage.toJSON(inputMessage),
                LogMessage.toJSON(storedMessage));
        Assert.assertEquals(inputMessage, storedMessage);
    }

    @Test
    public void testGetLogMessages() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);
        String messageId = UUID.randomUUID().toString();
        List<LogMessage> inputMessages = new ArrayList<LogMessage>();
        for (int i = 0; i < 10; i++) {

            LogMessage inputMessage = LogMessageTestModel.generate();
            inputMessage.setMessageId(messageId);

            storageService.store(inputMessage);

            inputMessages.add(inputMessage);
        }

        List<LogMessage> storedMessages = storageService
                .getLogMessages(messageId);

        Assert.assertEquals(10, storedMessages.size());

        int counter = 0;
        for (LogMessage storedMessage : storedMessages) {
            Assert.assertEquals(LogMessage.toJSON(inputMessages.get(counter)),
                    LogMessage.toJSON(storedMessage));
            Assert.assertEquals(inputMessages.get(counter), storedMessage);
            counter++;
        }

    }    */
}
