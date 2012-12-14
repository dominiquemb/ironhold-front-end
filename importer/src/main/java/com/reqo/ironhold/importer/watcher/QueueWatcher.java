package com.reqo.ironhold.importer.watcher;

import java.io.File;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.reqo.ironhold.importer.PSTImporter;
import com.reqo.ironhold.importer.notification.EmailNotification;
import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;

public class QueueWatcher extends FileWatcher {

	private static Logger logger = Logger.getLogger(QueueWatcher.class);
	private final IStorageService storageService;
	public QueueWatcher(String inputDirName, String outputDirName, String quarantineDirName, String client)
			throws Exception {
		super(inputDirName, outputDirName, quarantineDirName, client);
		storageService = new MongoService(this.getClient(), "PSTImporter");
	}

	@Override
	protected void processFile(File dataFile, MD5CheckSum checksumFile)
			throws Exception {
		logger.info("Processing data file " + dataFile.toString());
		
		PSTImporter importer = new PSTImporter(dataFile,
				checksumFile.getCheckSum(), checksumFile.getMailBoxName(),
				checksumFile.getOriginalFilePath(),
				checksumFile.getCommentary(), storageService);
		String details = importer.processMessages();
		
		EmailNotification.send("Finished processing pst file: " + checksumFile.getDataFileName(), details);
		
	}

	public static void main(String[] args) {
		WatcherOptions bean = new WatcherOptions();
		CmdLineParser parser = new CmdLineParser(bean);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			return;
		}
		try {
			QueueWatcher qw = new QueueWatcher(bean.getQueue(), bean.getOut(), bean.getQuarantine(), bean.getClient());
			qw.start();

		} catch (Exception e) {
			logger.error("Critical error detected. Exiting.", e);
			System.exit(0);
		}
	}

}
