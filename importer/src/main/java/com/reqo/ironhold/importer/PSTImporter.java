package com.reqo.ironhold.importer;

import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.reqo.ironhold.model.log.LogLevel;
import com.reqo.ironhold.model.log.LogMessage;
import com.reqo.ironhold.model.message.MessageSource;
import com.reqo.ironhold.model.message.pst.MailMessage;
import com.reqo.ironhold.model.message.pst.PSTFileMeta;
import com.reqo.ironhold.model.message.pst.PSTMessageSource;
import com.reqo.ironhold.storage.IStorageService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

public class PSTImporter {

	private static final int MILLIS_IN_A_SEC = 1000;

	private static final int INFO_BATCH_SIZE = 100;

	private static Logger logger = Logger.getLogger(PSTImporter.class);

	private final PSTFileMeta metaData;
	private final File file;
	private final IStorageService storageService;

	private String hostname;

	public PSTImporter(File file, String md5, String mailBoxName,
			String originalFilePath, String commentary,
			IStorageService storageService) throws UnknownHostException {
		this.file = file;
		this.hostname = InetAddress.getLocalHost().getHostName();
		this.metaData = new PSTFileMeta(file.getName(), mailBoxName,
				originalFilePath, commentary, md5, hostname, file.length(), new Date());
		this.storageService = storageService;

	}

	private boolean wasFileProcessedPreviously() throws Exception {
		for (PSTFileMeta pstFileMeta : storageService.getPSTFiles()) {
			if (pstFileMeta.sameAs(this.metaData)) {
				return true;
			}
		}

		return false;
	}

	public String processMessages() throws Exception {
		if (wasFileProcessedPreviously()) {
			throw new Exception("This file has been processed already");
		}

		PSTFile pstFile = new PSTFile(file);
		try {
			String fileSizeDisplay = FileUtils.byteCountToDisplaySize(file
					.length());

			LogMessage startedMessage = new LogMessage(LogLevel.Success,
					file.toString(), "Started pst import: File: ["
							+ file.toString() + "] File size: ["
							+ fileSizeDisplay + "]");

			storageService.store(startedMessage);

			long started = System.currentTimeMillis();

			processFolder("", pstFile.getRootFolder());

			metaData.setFinished(new Date());
			long finished = System.currentTimeMillis();

			String timeTook = DurationFormatUtils.formatDurationWords(finished
					- started, true, true);
			float duration = (finished - started) / MILLIS_IN_A_SEC;
			float rate = metaData.getMessages() / duration;

			String messageString = "Finished pst import: File: ["
					+ file.toString() + "] File size: [" + fileSizeDisplay
					+ "] Success " + "count: [" + metaData.getMessages()
					+ "] Duplicate count: [" + metaData.getDuplicates()
					+ "] Fail count: [" + metaData.getFailures() + "] Time "
					+ "taken: [" + timeTook + "] Rate: [" + rate
					+ " messages per sec]";
			LogMessage finishedMessage = new LogMessage(LogLevel.Success,
					file.toString(), messageString);

			storageService.store(finishedMessage);

			storageService.addPSTFile(metaData);

			return messageString;
		} finally {
			pstFile.getFileHandle().close();
		}

	}

	private void processFolder(String folderPath, PSTFolder folder)
			throws Exception {

		LogMessage folderMessage = new LogMessage(LogLevel.Success,
				file.toString(), "Processing " + folderPath + " ["
						+ folder.getContentCount() + " items]");
		storageService.store(folderMessage);
		// go through the folders...
		if (folder.hasSubfolders()) {
			List<PSTFolder> childFolders = folder.getSubFolders();
			for (PSTFolder childFolder : childFolders) {
				processFolder(folderPath + "/" + childFolder.getDisplayName(),
						childFolder);
			}
		}

		// and now the emails for this folder
		if (folder.getContentCount() > 0) {
			metaData.addFolder(folderPath, folder.getContentCount());

			PSTMessage message = (PSTMessage) folder.getNextChild();
			while (message != null) {
				String messageId = "unknown";

				try {

					messageId = message.getInternetMessageId();

					PSTMessageSource source = new PSTMessageSource(
							file.toString(), folderPath, file.length(),
							new Date(file.lastModified()));
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
					if (storageService.existsMailMessage(messageId)) {
						logger.warn("Found duplicate " + messageId);
						MailMessage storedMessage = storageService
								.getMailMessage(messageId);
						boolean newSource = true;
						for (MessageSource existingSource : storedMessage
								.getSources()) {
							if (existingSource instanceof PSTMessageSource
									&& PSTMessageSource.sameAs(
											(PSTMessageSource) existingSource,
											source)) {
								newSource = false;
								break;
							}

						}
						if (newSource) {
							storageService.addSource(messageId, source);
						}
						metaData.incrementDuplicates();
						if (metaData.getDuplicates() % INFO_BATCH_SIZE == 0) {
							logger.info("New Messages: "
									+ metaData.getMessages() + " Duplicates: "
									+ metaData.getDuplicates() + " Failures:"
									+ metaData.getFailures());
						}
					} else {

						if (metaData.getMessages() % INFO_BATCH_SIZE == 0) {
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

					storageService.store(processedMessage);
				} catch (Exception e) {
					LogMessage processedMessage = new LogMessage(
							LogLevel.Failure, messageId,
							"Failed to process message " + e.getMessage());

					storageService.store(processedMessage);
					metaData.incrementFailures();
				}
				message = (PSTMessage) folder.getNextChild();

			}
		}

	}

}
