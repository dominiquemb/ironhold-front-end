package com.reqo.ironhold.importer;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Date;
import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.reqo.ironhold.storage.model.PSTFileMeta;
import com.reqo.ironhold.storage.model.PSTMessageSource;

public class PSTImporter {

	private static Logger logger = Logger.getLogger(PSTImporter.class);

	private final PSTFileMeta metaData;
	private final File file;
	private final IStorageService storageService;
	private final String clientName;

	public PSTImporter(File file, String md5, String mailBoxName,
			String originalFilePath, String commentary, String clientName)
			throws Exception {
		this.file = file;
		this.clientName = clientName;
		this.metaData = new PSTFileMeta(file.getName(), mailBoxName,
				originalFilePath, commentary, md5, file.length(), new Date());
		this.storageService = new MongoService(clientName, "PST Importer");

	}

	public String processMessages() throws Exception {
		PSTFile pstFile = new PSTFile(file);
		try {
			String fileSizeDisplay = FileUtils.byteCountToDisplaySize(file
					.length());

			LogMessage startedMessage = new LogMessage(LogLevel.Success,
					file.toString(), "Started pst import: File: ["
							+ file.toString() + "] File size: ["
							+ fileSizeDisplay + "]");

			storageService.log(startedMessage);

			long started = System.currentTimeMillis();

			processFolder("", pstFile.getRootFolder());

			metaData.setFinished(new Date());
			long finished = System.currentTimeMillis();

			String timeTook = DurationFormatUtils.formatDurationWords(finished
					- started, true, true);
			float duration = (finished - started) / 1000;
			float rate = metaData.getMessages() / duration;

			String messageString = "Finished pst import: File: ["
					+ file.toString() + "] File size: ["
					+ fileSizeDisplay + "] Success " + "count: ["
					+ metaData.getMessages() + "] Duplicate count: ["
					+ metaData.getDuplicates() + "] Fail count: ["
					+ metaData.getFailures() + "] Time " + "taken: ["
					+ timeTook + "] Rate: [" + rate
					+ " messages per sec]";
			LogMessage finishedMessage = new LogMessage(LogLevel.Success,
					file.toString(), messageString);

			storageService.log(finishedMessage);

			storageService.addPSTFile(metaData);

			return messageString;
		} finally {
			pstFile.getFileHandle().close();
		}
		
	}

	private void processFolder(String folderPath, PSTFolder folder)
			throws Exception {
		metaData.addFolder(folderPath, folder.getContentCount());
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

				try {

					messageId = message.getInternetMessageId();

					PSTMessageSource source = new PSTMessageSource(
							file.toString(), folderPath, file.length(),
							new Date(file.lastModified()), new Date(),
							InetAddress.getLocalHost().getHostName());
					MailMessage mailMessage = new MailMessage(message, source);
					if (mailMessage.isPstPartialFailure()) {
						metaData.incrementPartialFailures();
					}

					metaData.incrementObjectType(mailMessage.getPstObjectType());
					metaData.updateSizeStatistics(
							MailMessage.serializeMailMessage(mailMessage)
									.length(),
							MailMessage.serializeCompressedMailMessage(
									mailMessage).length());
					metaData.incrementAttachmentStatistics(mailMessage
							.getAttachments().length > 0);
					if (mailMessage.getAttachments().length > 0) {
						metaData.updateAttachmentSizeStatistics(
								MailMessage.serializeAttachments(
										mailMessage.getAttachments()).length(),
								MailMessage.serializeCompressedAttachments(
										mailMessage.getAttachments()).length());
					}

					metaData.incrementMessages();
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
						metaData.incrementDuplicates();
						if (metaData.getDuplicates() % 100 == 0) {
							logger.info("New Messages: "
									+ metaData.getMessages() + " Duplicates: "
									+ metaData.getDuplicates() + " Failures:"
									+ metaData.getFailures());
						}
					} else {

						if (metaData.getMessages() % 100 == 0) {
							logger.info("New Messages: "
									+ metaData.getMessages() + " Duplicates: "
									+ metaData.getDuplicates() + " Failures:"
									+ metaData.getFailures());
						}
						storageService.store(mailMessage);
					}

					LogMessage processedMessage = new LogMessage(
							LogLevel.Success, messageId,
							"Processed pst message");

					storageService.log(processedMessage);
				} catch (Exception e) {
					LogMessage processedMessage = new LogMessage(
							LogLevel.Failure, messageId,
							"Failed to process message " + e.getMessage());

					storageService.log(processedMessage);
					metaData.incrementFailures();
				}
				message = (PSTMessage) folder.getNextChild();

			}
		}

	}

}
