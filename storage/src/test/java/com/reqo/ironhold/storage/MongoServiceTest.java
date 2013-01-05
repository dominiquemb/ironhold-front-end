package com.reqo.ironhold.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFS;
import com.reqo.ironhold.storage.model.Attachment;
import com.reqo.ironhold.storage.model.IMAPMessageSource;
import com.reqo.ironhold.storage.model.IndexStatus;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.LogMessageTestModel;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MailMessageTestModel;
import com.reqo.ironhold.storage.model.MessageSourceTestModel;
import com.reqo.ironhold.storage.model.MimeMailMessage;
import com.reqo.ironhold.storage.model.MimeMailMessageTestModel;
import com.reqo.ironhold.storage.model.PSTMessageSource;

import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.MongodProcess;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;

public class MongoServiceTest {
	private MongodExecutable mongodExe;
	private MongodProcess mongod;

	private Mongo mongo;
	private DB db;

	private static final String DATABASENAME = "MongoServiceTest";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		MongoDBRuntime runtime = MongoDBRuntime.getDefaultInstance();
		mongodExe = runtime
				.prepare(new MongodConfig(Version.V2_0, 12345, false));
		mongod = mongodExe.start();

		mongo = new Mongo("localhost", 12345);
		db = mongo.getDB(DATABASENAME);
	}

	@After
	public void tearDown() throws Exception {
		mongod.stop();
		mongodExe.cleanup();
	}

	@Test
	public void testExistsPositive() throws Exception {
		try {
			IStorageService storageService = new MongoService(mongo, db);

			MailMessage inputMessage = MailMessageTestModel
					.generatePSTMessage();

			storageService.store(inputMessage);

			MailMessageTestModel.verifyStorage(storageService, inputMessage);

			Assert.assertTrue(storageService.existsMailMessage(inputMessage.getMessageId()));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.assertNull(e);
		}
	}

	@Test
	public void testCompressedSerialization() throws JsonGenerationException,
			JsonMappingException, IOException {
		MailMessage inputMessage = MailMessageTestModel.generatePSTMessage();

		String serializedMessage = MailMessage
				.serializeCompressedMailMessage(inputMessage);
		MailMessage deserializedMessage = MailMessage
				.deserializeCompressedMailMessage(serializedMessage);

		Assert.assertEquals(inputMessage, deserializedMessage);

	}

	@Test
	public void testLargeMessage() throws Exception {
		try {
			IStorageService storageService = new MongoService(mongo, db);

			MailMessage inputMessage = MailMessageTestModel
					.generatePSTMessage();

			inputMessage.removeAttachments();

			Attachment attachment = new Attachment();
			StringBuilder sb = new StringBuilder();
			for (long i = 0; i < GridFS.MAX_CHUNKSIZE * 3; i++) {
				sb.append('a');
			}
			attachment.setBody(sb.toString());
			attachment.setCreationTime(new Date());
			attachment.setFileName("test.txt");
			attachment.setModificationTime(new Date());
			attachment.setSize((int) (GridFS.MAX_CHUNKSIZE * 3));
			inputMessage.addAttachment(attachment);

			storageService.store(inputMessage);

			MailMessageTestModel.verifyStorage(storageService, inputMessage);

			Assert.assertTrue(storageService.existsMailMessage(inputMessage.getMessageId()));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.assertNull(e);
		}
	}

	@Test
	public void testExistsNegative() throws Exception {
		IStorageService storageService = new MongoService(mongo, db);

		MailMessage inputMessage = MailMessageTestModel.generatePSTMessage();

		storageService.store(inputMessage);

		MailMessageTestModel.verifyStorage(storageService, inputMessage);

		Assert.assertFalse(storageService.existsMailMessage(UUID.randomUUID().toString()));
	}

	@Test
	public void testStore() throws Exception {
		IStorageService storageService = new MongoService(mongo, db);

		MailMessage inputMessage = MailMessageTestModel.generatePSTMessage();

		storageService.store(inputMessage);

		MailMessageTestModel.verifyStorage(storageService, inputMessage);
	}

	@Test
	public void testFindUnindexedPSTMessages() throws Exception {
		IStorageService storageService = new MongoService(mongo, db);

		int storedCount = 0;
		for (MailMessage inputMessage : MailMessageTestModel
				.generatePSTMessages()) {

			storageService.store(inputMessage);

			MailMessageTestModel.verifyStorage(storageService, inputMessage);
			storedCount++;
		}

		List<MailMessage> unindexedMessages = storageService
				.findUnindexedPSTMessages(100);
		Assert.assertEquals(storedCount, unindexedMessages.size());

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

		MailMessage inputMessage = MailMessageTestModel.generatePSTMessage();

		storageService.store(inputMessage);

		MailMessageTestModel.verifyStorage(storageService, inputMessage);

		PSTMessageSource source = MessageSourceTestModel
				.generatePSTMessageSource();
		storageService.addSource(inputMessage.getMessageId(), source);

		inputMessage.addSource(source);

		MailMessageTestModel.verifyStorage(storageService, inputMessage);

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
	public void testPSTMarkAsIndexed() throws Exception {
		IStorageService storageService = new MongoService(mongo, db);

		MailMessage inputMessage = MailMessageTestModel.generatePSTMessage();

		storageService.store(inputMessage);

		MailMessageTestModel.verifyStorage(storageService, inputMessage);


		List<MailMessage> unindexedMessages1 = storageService.findUnindexedPSTMessages(100);
		Assert.assertEquals(1, unindexedMessages1.size());

		storageService.updateIndexStatus(inputMessage, IndexStatus.INDEXED);

		inputMessage.setIndexed(IndexStatus.INDEXED);

		MailMessageTestModel.verifyStorage(storageService, inputMessage);

		List<MailMessage> unindexedMessages2 = storageService.findUnindexedPSTMessages(100);
		Assert.assertEquals(0, unindexedMessages2.size());
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

		List<MimeMailMessage> unindexedMessages1 = storageService.findUnindexedIMAPMessages(100);
		Assert.assertEquals(1, unindexedMessages1.size());
		
		storageService.updateIndexStatus(inputMessage, IndexStatus.INDEXED);

		inputMessage.setIndexed(IndexStatus.INDEXED);

		MimeMailMessageTestModel.verifyStorage(storageService, inputMessage);
		
		List<MimeMailMessage> unindexedMessages2 = storageService.findUnindexedIMAPMessages(100);
		Assert.assertEquals(0, unindexedMessages2.size());

	}

	@Test
	public void testGetTotalMessageCount() throws Exception {
		IStorageService storageService = new MongoService(mongo, db);

		List<MailMessage> messages = MailMessageTestModel.generatePSTMessages();
		for (MailMessage inputMessage : messages) {
			storageService.store(inputMessage);

			MailMessageTestModel.verifyStorage(storageService, inputMessage);
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
