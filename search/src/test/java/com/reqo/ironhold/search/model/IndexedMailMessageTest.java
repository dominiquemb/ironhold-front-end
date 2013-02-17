package com.reqo.ironhold.search.model;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.MailMessage;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class IndexedMailMessageTest {

	private MongodExecutable mongodExe;
	private MongodProcess mongod;
	private Mongo mongo;
	private DB db;
	private MailMessageTestModel testModel;
	private static final String DATABASENAME = "mongo_test";

	@Before
	public void setUp() throws Exception {
		MongodStarter runtime = MongodStarter.getDefaultInstance();
		mongodExe = runtime.prepare(new MongodConfig(Version.Main.V2_0, 12345,
				Network.localhostIsIPv6()));
		mongod = mongodExe.start();
		mongo = new Mongo("localhost", 12345);
		db = mongo.getDB(DATABASENAME);

		testModel = new MailMessageTestModel("/data.pst");
	}

	@After
	public void tearDown() throws Exception {
		mongod.stop();
		mongodExe.stop();
	}

	@Test
	public void testIndexedMailMessageConstructor() throws Exception {
		try {
			IStorageService storageService = new MongoService(mongo, db);

			MailMessage inputMessage = testModel.generatePSTMessage();

			storageService.store(inputMessage);

			testModel.verifyStorage(storageService, inputMessage);

			Assert.assertTrue(storageService.existsMailMessage(inputMessage
					.getMessageId()));

			MailMessage storedMessage = storageService
					.getMailMessage(inputMessage.getMessageId());

			IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
					storedMessage);

			Assert.assertEquals(storedMessage.getPstMessage()
					.getInternetMessageId(), indexedMailMessage.getMessageId());
			Assert.assertEquals(storedMessage.getPstMessage().getBody(),
					indexedMailMessage.getBody());
			Assert.assertEquals(storedMessage.getPstMessage().getSubject(),
					indexedMailMessage.getSubject());
			Assert.assertEquals(storedMessage.getPstMessage()
					.getMessageDeliveryTime(), indexedMailMessage
					.getMessageDate());
			Assert.assertEquals(storedMessage.getPstMessage()
					.getSenderEmailAddress(), indexedMailMessage.getSender()
					.getAddress());
			Assert.assertEquals(storedMessage.getPstMessage().getMessageSize(),
					indexedMailMessage.getSize());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.assertNull(e);
		}
	}

}
