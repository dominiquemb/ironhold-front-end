package com.reqo.ironhold.search.model;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.Mongo;

import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFS;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.*;

import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.MongodProcess;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;
import junit.framework.Assert;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class IndexedMailMessageTest {

	private MongodExecutable mongodExe;
	private MongodProcess mongod;
	private Mongo mongo;
	private DB db;
	private static final String DATABASENAME = "mongo_test";

	@Before
	public void setUp() throws Exception {

		MongoDBRuntime runtime = MongoDBRuntime.getDefaultInstance();
		mongodExe = runtime
				.prepare(new MongodConfig(Version.V2_0, 12346, false));
		mongod = mongodExe.start();

		mongo = new Mongo("localhost", 12346);
		db = mongo.getDB(DATABASENAME);
	}

	@After
	public void tearDown() throws Exception {
		mongod.stop();
		mongodExe.cleanup();
	}

	@Test
	public void testIndexedMailMessageConstructor() throws Exception {
		try {
			IStorageService storageService = new MongoService(mongo, db);

			MailMessage inputMessage = MailMessageTestModel
					.generatePSTMessage();

			storageService.store(inputMessage);

			MailMessageTestModel.verifyStorage(storageService, inputMessage);

			Assert.assertTrue(storageService.exists(inputMessage.getMessageId()));

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
			Assert.assertEquals(storedMessage.getPstMessage().getSenderName(),
					indexedMailMessage.getSender().getName());
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
