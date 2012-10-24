package com.reqo.ironhold.importer;

import java.io.File;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.LogLevel;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MailMessage;


public class PSTImporter {
	

	private static Logger logger = Logger.getLogger(PSTImporter.class);

	public static void main(String[] args) {
		PSTImporterOptions bean = new PSTImporterOptions();
		CmdLineParser parser = new CmdLineParser(bean);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			return;
		}
		try {

			PSTImporter importer = new PSTImporter();

			importer.processMessages(bean.getFile(), bean.getClient());

		} catch (Exception e) {
			logger.error("Critical error detected. Exiting.", e);
			System.exit(0);
		}
	}

	private int count;
	private int fail;
	private int duplicate;
	private File file;
	private IStorageService storageService;

	private void processMessages(File file, String clientName) throws Exception {
		this.file = file;
		PSTFile pstFile = new PSTFile(file);

		storageService = new MongoService(clientName, "PST Importer");
		String fileSizeDisplay = FileUtils
				.byteCountToDisplaySize(file.length());

		LogMessage startedMessage = new LogMessage(LogLevel.Success,
				file.toString(), "Started pst import: File: ["
						+ file.toString() + "] File size: [" + fileSizeDisplay
						+ "]");

		storageService.log(startedMessage);
		count = 0;
		fail = 0;
		duplicate = 0;

		long started = System.currentTimeMillis();

		processFolder(pstFile.getRootFolder());
		long finished = System.currentTimeMillis();

		String timeTook = DurationFormatUtils.formatDurationWords(finished
				- started, true, true);
		float duration = (finished - started) / 1000;
		float rate = (count + fail + duplicate) / duration;

		LogMessage finishedMessage = new LogMessage(LogLevel.Success,
				file.toString(), "Finished pst import: File: ["
						+ file.toString() + "] File size: [" + fileSizeDisplay
						+ "] Success count: [" + count + "] Skipped count: ["
						+ duplicate + "] Fail count: [" + fail
						+ "] Time taken: [" + timeTook + "] Rate: [" + rate
						+ " messages per sec]");

		storageService.log(finishedMessage);
	}

	public void processFolder(PSTFolder folder) throws Exception {

		// go through the folders...
		if (folder.hasSubfolders()) {
			Vector<PSTFolder> childFolders = folder.getSubFolders();
			for (PSTFolder childFolder : childFolders) {
				processFolder(childFolder);
			}
		}

		// and now the emails for this folder
		if (folder.getContentCount() > 0) {
			PSTMessage message = (PSTMessage) folder.getNextChild();
			while (message != null) {
				String messageId = "unknown";

				messageId = message.getInternetMessageId();
				MailMessage mailMessage = new MailMessage(storageService, message,
						file.toString() + ":" + folder.getDisplayName());
				if (storageService.exists(messageId)) {
					storageService.addSource(messageId, file.toString() + ":"
							+ folder.getDisplayName());
					duplicate++;
					if (duplicate % 100 == 0) {
						logger.info("New Messages: " + count + " Duplicates: "
								+ duplicate + " Failures:" + fail);
					}
				} else {
					count++;
					if (count % 100 == 0) {
						logger.info("New Messages: " + count + " Duplicates: "
								+ duplicate + " Failures:" + fail);
					}
					storageService.store(mailMessage);
				}

				LogMessage processedMessage = new LogMessage(LogLevel.Success,
						messageId, "Processed message");

				storageService.log(processedMessage);

				message = (PSTMessage) folder.getNextChild();
			}
		}
	}
}
