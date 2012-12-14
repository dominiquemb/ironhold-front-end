package com.req.ironhold.importer.watcher;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.reqo.ironhold.importer.notification.EmailNotification;
import com.reqo.ironhold.importer.watcher.InboundWatcher;
import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;

public class InboundWatcherTest {

	private static final String client = "test client";
	private static final String PST_TEST_FILE = "/data.pst";

	@Rule
	public TemporaryFolder inFolder = new TemporaryFolder();

	@Rule
	public TemporaryFolder queueFolder = new TemporaryFolder();

	@Rule
	public TemporaryFolder outFolder = new TemporaryFolder();

	@Rule
	public TemporaryFolder quarantineFolder = new TemporaryFolder();

	@Test
	public void test() throws Exception {
		EmailNotification.disableNotification();
		final InboundWatcher iw = new InboundWatcher(inFolder.getRoot().toString(),
				queueFolder.getRoot().toString(), quarantineFolder
				.getRoot().toString(), client);;
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.execute(new Runnable() {

			@Override
			public void run() {

				try {
					iw.start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		File pstFile = FileUtils.toFile(InboundWatcherTest.class
				.getResource(PST_TEST_FILE));

		FileUtils.copyFileToDirectory(pstFile, inFolder.getRoot());
		File md5File = MD5CheckSum.createMD5CheckSum(pstFile);
		FileUtils.copyFileToDirectory(md5File, inFolder.getRoot());
		Thread.sleep(2000);

		Assert.assertEquals(0, inFolder.getRoot().listFiles().length);
		Assert.assertEquals(2, queueFolder.getRoot().listFiles().length);

		iw.deActivate();
		executorService.shutdown();

		while (!executorService.isTerminated()) {
			executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
		}

	}

}
