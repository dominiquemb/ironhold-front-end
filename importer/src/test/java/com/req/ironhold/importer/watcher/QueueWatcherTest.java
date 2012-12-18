package com.req.ironhold.importer.watcher;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.reqo.ironhold.importer.PSTImporterTest;
import com.reqo.ironhold.importer.notification.EmailNotification;
import com.reqo.ironhold.importer.watcher.InboundWatcher;
import com.reqo.ironhold.importer.watcher.QueueWatcher;
import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;

import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.MongodProcess;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;

public class QueueWatcherTest {

	private static final String client = "test client";
	private static final String PST_TEST_FILE = "/data.pst";

	private MongodExecutable mongodExe;
	private MongodProcess mongod;

	private Mongo mongo;
	private DB db;

	private static final String DATABASENAME = "TestPSTImporter";

	@Rule
	public TemporaryFolder inFolder = new TemporaryFolder();

	@Rule
	public TemporaryFolder queueFolder = new TemporaryFolder();

	@Rule
	public TemporaryFolder outFolder = new TemporaryFolder();

	@Rule
	public TemporaryFolder quarantineFolder = new TemporaryFolder();

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
	public void testQueueWatcherValid() throws Exception {
		IStorageService storageService = new MongoService(mongo, db);
		EmailNotification.disableNotification();
		final QueueWatcher qw = new QueueWatcher(queueFolder.getRoot().toString(),
				outFolder.getRoot().toString(), quarantineFolder
				.getRoot().toString(), client, storageService);
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.execute(new Runnable() {

			@Override
			public void run() {

				try {
					qw.start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		
		/* Make sure watcher is fully started */
		while (!qw.isStarted()) {
			Thread.sleep(100);
		}
		Thread.sleep(100);
		
		
		File pstFile = FileUtils.toFile(QueueWatcherTest.class
				.getResource(PST_TEST_FILE));

		FileUtils.copyFileToDirectory(pstFile, queueFolder.getRoot());
		File md5File = MD5CheckSum.createMD5CheckSum(pstFile);
		FileUtils.copyFileToDirectory(md5File, queueFolder.getRoot());

		Thread.sleep(10000);

		Assert.assertEquals(0, queueFolder.getRoot().listFiles().length);
		Assert.assertEquals(2, outFolder.getRoot().listFiles().length);

		qw.deActivate();
		executorService.shutdown();

		while (!executorService.isTerminated()) {
			executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
		}

	}

}