package com.reqo.ironhold.importer.watcher;

import com.reqo.ironhold.importer.PSTImporter;
import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;

public class QueueWatcher extends FileWatcher {

	private static Logger logger = Logger.getLogger(QueueWatcher.class);

	public QueueWatcher(String inputDirName, String outputDirName, String quarantineDirName, String client)
			throws Exception {
		super(inputDirName, outputDirName, quarantineDirName, client);
	}

	@Override
	protected void processFile(File dataFile, MD5CheckSum checksumFile)
			throws Exception {
		logger.info("Processing data file " + dataFile.toString());
		PSTImporter importer = new PSTImporter(dataFile,
				checksumFile.getCheckSum(), checksumFile.getMailBoxName(),
				checksumFile.getOriginalFilePath(),
				checksumFile.getCommentary(), this.getClient());
		String details = importer.processMessages();
		
		send("Finished processing pst file: " + checksumFile.getDataFileName(), details);
		
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
			new QueueWatcher(bean.getQueue(), bean.getOut(), bean.getQuarantine(), bean.getClient());

		} catch (Exception e) {
			logger.error("Critical error detected. Exiting.", e);
			System.exit(0);
		}
	}

}
