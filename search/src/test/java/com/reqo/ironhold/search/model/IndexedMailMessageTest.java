package com.reqo.ironhold.search.model;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.reqo.ironhold.model.message.pst.MailMessage;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.testcommon.MailMessageTestModel;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class IndexedMailMessageTest {

    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private Mongo mongo;
    private DB db;
    private MailMessageTestModel testModel;
    private static final String DATABASENAME = "IndexedMailMessageTest";

    @Before
    public void setUp() throws Exception {
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        mongodExe = runtime.prepare(new MongodConfig(Version.Main.V2_0, 12345,
                Network.localhostIsIPv6()));
        mongod = mongodExe.start();
        mongo = new Mongo("localhost", 12345);
        db = mongo.getDB(DATABASENAME);

        testModel = new MailMessageTestModel("/attachments.pst");
    }

    @After
    public void tearDown() throws Exception {
        mongod.stop();
        mongodExe.stop();
    }

    @Test
    public void testIndexedMailMessageConstructor() throws Exception {

        IStorageService storageService = new MongoService(mongo, db);

        List<MailMessage> inputMessages = testModel.generatePSTMessages();

        for (MailMessage inputMessage : inputMessages) {
            storageService.store(inputMessage);

            MailMessage storedMessage = storageService.getMailMessage(
                    inputMessage.getMessageId(), true);

            testModel.verifyStorage(storedMessage, inputMessage);

            Assert.assertTrue(storageService.existsMailMessage(inputMessage
                    .getMessageId()));

            MailMessage storedMessage2 = storageService
                    .getMailMessage(inputMessage.getMessageId());

            IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                    storedMessage2);

            Assert.assertEquals(storedMessage2.getPstMessage()
                    .getInternetMessageId(), indexedMailMessage.getMessageId());
            Assert.assertEquals(storedMessage2.getPstMessage().getBody(),
                    indexedMailMessage.getBody());
            Assert.assertEquals(storedMessage2.getPstMessage().getSubject(),
                    indexedMailMessage.getSubject());
            Assert.assertEquals(storedMessage2.getPstMessage()
                    .getMessageDeliveryTime(), indexedMailMessage
                    .getMessageDate());
            Assert.assertEquals(storedMessage2.getPstMessage()
                    .getSenderEmailAddress(), indexedMailMessage.getSender()
                    .getAddress());
            Assert.assertEquals(storedMessage2.getPstMessage().getMessageSize(),
                    indexedMailMessage.getSize());

            Assert.assertEquals(storedMessage2.getAttachments().length, indexedMailMessage.getAttachments().length);

            for (int i = 0; i < storedMessage2.getAttachments().length; i++) {
                Assert.assertEquals(storedMessage2.getAttachments()[i].getFileExt(), indexedMailMessage.getAttachments()[i].getFileExt());
                Assert.assertEquals(storedMessage2.getAttachments()[i].getFileName(), indexedMailMessage.getAttachments()[i].getFileName());
                Assert.assertEquals(storedMessage2.getAttachments()[i].getSize(), indexedMailMessage.getAttachments()[i].getSize());
            }
        }

    }

}
