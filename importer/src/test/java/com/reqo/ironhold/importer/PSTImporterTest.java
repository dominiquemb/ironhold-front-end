package com.reqo.ironhold.importer;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.PSTFileMeta;

import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.MongodProcess;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;

public class PSTImporterTest {
	private MongodExecutable mongodExe;
	private MongodProcess mongod;

	private Mongo mongo;
	private DB db;

	private static final String DATABASENAME = "TestPSTImporter";

	private static final String PST_TEST_FILE = "/data.pst";
	private static final String mailBoxName = "test mailbox";
	private static final String originalFilePath = "test path";
	private static final String commentary = "test commentary";
	private File pstfile;
	private String md5;

	@Before
	public void setUp() throws Exception {
		pstfile = FileUtils.toFile(PSTImporterTest.class
				.getResource(PST_TEST_FILE));

		md5 = MD5CheckSum.getMD5Checksum(pstfile);

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
	public void testPSTImporter() throws Exception {
		IStorageService storageService = new MongoService(mongo, db);
		PSTImporter pstImporter = new PSTImporter(pstfile, md5, mailBoxName,
				originalFilePath, commentary, storageService);

		String results = pstImporter.processMessages();

		System.out.println(results);

		List<PSTFileMeta> pstFiles = storageService.getPSTFiles();

		Assert.assertEquals(1, pstFiles.size());

		PSTFileMeta pstFileMeta = pstFiles.get(0);
		Assert.assertNotNull(pstFileMeta);
		Assert.assertEquals(0, pstFileMeta.getFailures());

		long totalCount = storageService.getTotalMessageCount();
		Assert.assertEquals(
				pstFileMeta.getMessages() - pstFileMeta.getDuplicates(),
				totalCount);
		
	/*	for (String folder : pstFileMeta.getFolderMap().keySet()) {
			System.out.println(folder + "  : " + pstFileMeta.getFolderMap().get(folder));
		}*/
		
		Assert.assertEquals(1L, pstFileMeta.getFolderMap().get("/Top of Outlook data file/Inbox/Resumes/data/sent test").longValue());
		Assert.assertEquals(7L, pstFileMeta.getFolderMap().get("/Top of Outlook data file/Inbox/Resumes/data/in person.interview".replace(".", PSTFileMeta.DOT_REPLACEMENT)).longValue());
		Assert.assertEquals(6L, pstFileMeta.getFolderMap().get("/Top of Outlook data file/Inbox/Resumes/data").longValue());
		Assert.assertEquals(2L, pstFileMeta.getFolderMap().get("/Top of Outlook data file/Inbox/Resumes/data/Withdrew").longValue());
		Assert.assertEquals(30L, pstFileMeta.getFolderMap().get("/Top of Outlook data file/Inbox/Resumes/data/reject after test").longValue());
		Assert.assertEquals(1L, pstFileMeta.getFolderMap().get("/Top of Outlook data file/Inbox/Resumes/data/recieved test").longValue());
		
		MailMessage testMailMessage = storageService.getMailMessage("<984867.51882.qm@web30305.mail.mud.yahoo.com>");
		Assert.assertNotNull(testMailMessage);
		Assert.assertEquals(1, testMailMessage.getSources().length);
	}

	@Test
	public void testPSTImporterDupes() throws Exception {
		testPSTImporter();
		IStorageService storageService = new MongoService(mongo, db);
		PSTImporter pstImporter = new PSTImporter(pstfile, md5, mailBoxName,
				originalFilePath, commentary, storageService);

		String results = pstImporter.processMessages();

		System.out.println(results);

		List<PSTFileMeta> pstFiles = storageService.getPSTFiles();

		Assert.assertEquals(2, pstFiles.size());

		PSTFileMeta pstFileMeta0 = pstFiles.get(0);
		PSTFileMeta pstFileMeta1 = pstFiles.get(1);
		Assert.assertNotNull(pstFileMeta1);
		Assert.assertEquals(0, pstFileMeta1.getFailures());

		long totalCount = storageService.getTotalMessageCount();
		
		Assert.assertEquals(
				pstFileMeta1.getDuplicates() - pstFileMeta0.getDuplicates(),
				totalCount);
		
		MailMessage testMailMessage = storageService.getMailMessage("<984867.51882.qm@web30305.mail.mud.yahoo.com>");
		Assert.assertNotNull(testMailMessage);
		Assert.assertEquals(1, testMailMessage.getSources().length);

	}

}
