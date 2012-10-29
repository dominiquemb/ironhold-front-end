package com.reqo.ironhold.watcher;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.reqo.ironhold.importer.PSTImporter;
import com.reqo.ironhold.watcher.checksum.MD5CheckSum;

public class InboundWatcher extends FileWatcher {
	private static Logger logger = Logger.getLogger(InboundWatcher.class);

	public InboundWatcher(String inputDirName, String outputDirName)
			throws Exception {
		super(inputDirName, outputDirName);
	}


	@Override
	protected void processFile(File dataFile) throws Exception {
		logger.info("Queuing valid file " + dataFile.toString());
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
			new InboundWatcher(bean.getIn(), bean.getQueue());
		} catch (Exception e) {
			logger.error("Critical error detected. Exiting.", e);
			System.exit(0);
		}
	}
}
