package com.reqo.ironhold.storage;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFS;
import com.pff.PSTException;
import com.pff.PSTMessage;
import com.reqo.ironhold.storage.model.*;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.junit.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MongoServiceTest {
    private MongodExecutable mongodExe;
    private MongodProcess mongod;

    private Mongo mongo;
    private DB db;
    private PSTMessageTestModel testModel;
    private static final String DATABASENAME = "MongoServiceTest";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        mongodExe = runtime.prepare(new MongodConfig(Version.Main.V2_0, 12345,
                Network.localhostIsIPv6()));
        mongod = mongodExe.start();
        mongo = new Mongo("localhost", 12345);
        db = mongo.getDB(DATABASENAME);

        testModel = new PSTMessageTestModel("/data.pst");
    }

    @After
    public void tearDown() throws Exception {
        mongod.stop();
        mongodExe.stop();
    }

    @Test
    public void testExistsPositive() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());
        inputMessage.addSource(MessageSourceTestModel
                .generatePSTMessageSource());


        storageService.store(inputMessage);

        MimeMailMessageTestModel.verifyStorage(storageService, inputMessage);

        Assert.assertTrue(storageService.existsMimeMailMessage(inputMessage
                .getMessageId()));
    }

    @Test
    public void testAttachmentsAbsence() throws IOException, PSTException, MessagingException, EmailException {
        testModel = new PSTMessageTestModel("/data.pst");

        MimeMailMessage pstMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        Assert.assertTrue(pstMessage.getAttachments().length == 0);


    }

    @Test
    public void testAttachmentsPresence() throws IOException, PSTException, MessagingException, EmailException {
        testModel = new PSTMessageTestModel("/attachments.pst");

        MimeMailMessage pstMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        Assert.assertTrue(pstMessage.getAttachments().length > 0);

        String serializedMessage = MimeMailMessage.serialize(pstMessage);

        Assert.assertFalse(serializedMessage.contains("\"attachments\":"));

    }


    @Test
    public void testAttachmentsSerialization() throws IOException, PSTException, MessagingException, EmailException {
        testModel = new PSTMessageTestModel("/attachments.pst");

        PSTMessage originalPSTMessage = testModel.generateOriginalPSTMessage();
        MimeMailMessage pstMessage = MimeMailMessage.getMimeMailMessage(originalPSTMessage);

        Assert.assertTrue(pstMessage.getAttachments().length > 0);

        Attachment[] attachments = pstMessage.getAttachments();

        Assert.assertTrue(attachments.length > 0);

        Assert.assertEquals(originalPSTMessage.getNumberOfAttachments(), pstMessage.getAttachments().length);

        for (int i = 0; i < pstMessage.getAttachments().length; i++) {
            Assert.assertEquals(originalPSTMessage.getAttachment(i).getLongFilename(), pstMessage.getAttachments()[i].getFileName());
        }
    }

    @Test
    public void testLargeMessage() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

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


        mimeMailMessage.addSource(MessageSourceTestModel
                .generatePSTMessageSource());
        storageService.store(mimeMailMessage);

        MimeMailMessageTestModel.verifyStorage(storageService, mimeMailMessage);

        Assert.assertTrue(storageService.existsMimeMailMessage(mimeMailMessage
                .getMessageId()));
    }

    @Test
    public void testExistsNegative() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());
        inputMessage.addSource(MessageSourceTestModel
                .generatePSTMessageSource());
        storageService.store(inputMessage);

        MimeMailMessageTestModel.verifyStorage(storageService, inputMessage);

        Assert.assertFalse(storageService.existsMimeMailMessage(UUID.randomUUID()
                .toString()));
    }

    @Test
    public void testStore() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());
        inputMessage.addSource(MessageSourceTestModel
                .generatePSTMessageSource());

        storageService.store(inputMessage);

        MimeMailMessageTestModel.verifyStorage(storageService, inputMessage);
    }


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

    }
}
