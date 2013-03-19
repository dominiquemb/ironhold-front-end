package com.reqo.ironhold.search.model;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.MimeMailMessage;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IndexedMailMessageTest {

    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private Mongo mongo;
    private DB db;
    private PSTMessageTestModel testModel;
    private static final String DATABASENAME = "mongo_test";

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
    public void testIndexedMailMessageConstructor() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());
        inputMessage.addSource(MessageSourceTestModel.generatePSTMessageSource());
        storageService.store(inputMessage);

        MimeMailMessageTestModel.verifyStorage(storageService, inputMessage);

        Assert.assertTrue(storageService.existsMimeMailMessage(inputMessage
                .getMessageId()));

        MimeMailMessage storedMessage = storageService
                .getMimeMailMessage(inputMessage.getMessageId());

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                storedMessage);

        Assert.assertEquals(storedMessage
                .getMessageId(), indexedMailMessage.getMessageId());

        Assert.assertEquals(StringUtils.deleteWhitespace(storedMessage.getBody()),
                StringUtils.deleteWhitespace(indexedMailMessage.getBody()));
        Assert.assertEquals(storedMessage.getSubject(),
                indexedMailMessage.getSubject());
        Assert.assertEquals(storedMessage
                .getMessageDate(), indexedMailMessage
                .getMessageDate());
        Assert.assertEquals(storedMessage
                .getFrom().getAddress(), indexedMailMessage.getSender()
                .getAddress());
        Assert.assertEquals(storedMessage.getSize(),
                indexedMailMessage.getSize());

    }


}
