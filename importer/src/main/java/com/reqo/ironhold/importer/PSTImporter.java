package com.reqo.ironhold.importer;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Date;
import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.reqo.ironhold.importer.mbeans.MongoMBean;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.LogLevel;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MessageSource;
import com.reqo.ironhold.storage.model.PSTMessageSource;

public class PSTImporter implements MongoMBean {

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
	private int monitorDuplicates;
	private int monitorProcessed;

	public PSTImporter() {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

			ObjectName name = new ObjectName(
					"com.reqo.ironhold.importer:type=MongoMBean");

			mbs.registerMBean(this, name);
		} catch (Exception e) {
			logger.warn("Failed to register mbean", e);
		}

	}

	public void processMessages(File file, String clientName) throws Exception {
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

		processFolder("", pstFile.getRootFolder());
		long finished = System.currentTimeMillis();

		String timeTook = DurationFormatUtils.formatDurationWords(finished
				- started, true, true);
		float duration = (finished - started) / 1000;
		float rate = (count + fail + duplicate) / duration;

		LogMessage finishedMessage = new LogMessage(LogLevel.Success,
				file.toString(), "Finished pst import: File: ["
						+ file.toString() + "] File size: [" + fileSizeDisplay
						+ "] Success " + "count: [" + count
						+ "] Duplicate count: [" + duplicate
						+ "] Fail count: [" + fail + "] Time " + "taken: ["
						+ timeTook + "] Rate: [" + rate + " messages per sec]");

		storageService.log(finishedMessage);
	}

	private void processFolder(String folderPath, PSTFolder folder)
			throws Exception {
		LogMessage folderMessage = new LogMessage(LogLevel.Success,
				file.toString(), "Processing " + folderPath + " ["
						+ folder.getContentCount() + " items]");
		storageService.log(folderMessage);
		// go through the folders...
		if (folder.hasSubfolders()) {
			Vector<PSTFolder> childFolders = folder.getSubFolders();
			for (PSTFolder childFolder : childFolders) {
				processFolder(folderPath + "/" + childFolder.getDisplayName(),
						childFolder);
			}
		}

		// and now the emails for this folder
		if (folder.getContentCount() > 0) {
			PSTMessage message = (PSTMessage) folder.getNextChild();
			while (message != null) {
				String messageId = "unknown";

				messageId = message.getInternetMessageId();
				/*
				 * MailMessage mailMessage = new MailMessage(storageService,
				 * message, file.toString() + ":" + folder.getDisplayName());
				 */
				PSTMessageSource source = new PSTMessageSource(file.toString(),
						folderPath, file.length(),
						new Date(file.lastModified()), new Date(), InetAddress
								.getLocalHost().getHostName());
				MailMessage mailMessage = new MailMessage(message, source);
				if (storageService.exists(messageId)) {
					logger.warn("Found duplicate " + messageId);
					MailMessage storedMessage = storageService
							.getMailMessage(messageId);
					boolean newSource = true;
					for (MessageSource existingSource : storedMessage
							.getSources()) {
						if (existingSource.equals(source)) {
							newSource = false;
							break;
						}
					}
					if (newSource) {
						storageService.addSource(messageId, source);
					}
					monitorDuplicates++;
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
					monitorProcessed++;
				}

				LogMessage processedMessage = new LogMessage(LogLevel.Success,
						messageId, "Processed pst message");

				storageService.log(processedMessage);

				message = (PSTMessage) folder.getNextChild();
			}
		}
	}

	@Override
	public int getProcessed() {
		try {
			return monitorProcessed;
		} finally {
			monitorProcessed = 0;
		}
	}

	@Override
	public int getDuplicates() {
		try {
			return monitorDuplicates;
		} finally {
			monitorDuplicates = 0;
		}
	}
}
