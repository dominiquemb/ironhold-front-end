package com.reqo.ironhold.importer;

import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MessageMetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.log.LogLevel;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.PSTMessageSource;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;
import com.reqo.ironhold.storage.model.search.IndexFailure;
import com.reqo.ironhold.storage.model.search.IndexedMailMessage;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

public class PSTImporter {

    private static final int MILLIS_IN_A_SEC = 1000;

    private static final int INFO_BATCH_SIZE = 100;

    private static Logger logger = Logger.getLogger(PSTImporter.class);

    @Autowired
    private MiscIndexService miscIndexService;

    @Autowired
    private MessageMetaDataIndexService messageMetaDataIndexService;

    @Autowired
    private IMimeMailMessageStorageService mimeMailMessageStorageService;

    @Autowired
    private MessageIndexService messageIndexService;

    private PSTFileMeta metaData;

    private File file;
    private String client;
    private final String hostname;
    private String md5;
    private String mailBoxName;
    private String originalFilePath;
    private String commentary;

    public PSTImporter() throws UnknownHostException {
        this.hostname = InetAddress.getLocalHost().getHostName();

    }

    private boolean wasFileProcessedPreviously() throws Exception {
        return miscIndexService.exists(client, this.metaData);
    }

    public String processMessages() throws Exception {
        this.metaData = new PSTFileMeta(file.getName(), mailBoxName,
                originalFilePath, commentary, md5, hostname, file.length(), new Date());

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

            messageMetaDataIndexService.store(client, startedMessage);

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


            messageMetaDataIndexService.store(client, finishedMessage);
            miscIndexService.store(client, metaData);


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
        messageMetaDataIndexService.store(client, folderMessage);

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

                    MimeMailMessage mimeMailMessage = MimeMailMessage.getMimeMailMessage(message);

                    if (mimeMailMessageStorageService.exists(client, mimeMailMessage.getPartition(), mimeMailMessage.getMessageId())) {
                        logger.warn("Found duplicate " + messageId);
                        metaData.incrementDuplicates();


                        if (metaData.getDuplicates() % INFO_BATCH_SIZE == 0) {
                            logger.info("New Messages: "
                                    + metaData.getMessages() + " Duplicates: "
                                    + metaData.getDuplicates() + " Failures:"
                                    + metaData.getFailures());
                        }
                    } else {
                        logger.info("New message " + messageId);
                        if (metaData.getMessages() % INFO_BATCH_SIZE == 0) {
                            logger.info("New Messages: "
                                    + metaData.getMessages() + " Duplicates: "
                                    + metaData.getDuplicates() + " Failures:"
                                    + metaData.getFailures());
                        }
                        long storedSize = mimeMailMessageStorageService.store(client, mimeMailMessage.getPartition(), mimeMailMessage.getMessageId(), mimeMailMessage.getRawContents(), CheckSumHelper.getCheckSum(mimeMailMessage.getRawContents().getBytes()));
                        metaData.updateSizeStatistics(mimeMailMessage.getRawContents().length(), storedSize);
                    }

                    PSTMessageSource source = new PSTMessageSource(messageId,
                            file.toString(), folderPath, file.length(),
                            new Date(file.lastModified()));

                    messageMetaDataIndexService.store(client, source);
                    metaData.incrementObjectType(message.getClass().getSimpleName());

                    metaData.incrementAttachmentStatistics(mimeMailMessage.isHasAttachments());

                    metaData.incrementMessages();
                    LogMessage processedMessage = new LogMessage(
                            LogLevel.Success, messageId,
                            "Processed pst message");
                    messageMetaDataIndexService.store(client, processedMessage);

                    try {
                        messageIndexService.store(client, new IndexedMailMessage(mimeMailMessage));
                    } catch(Exception e) {
                        logger.error("Failed to index message " + mimeMailMessage.getMessageId(), e);
                        messageMetaDataIndexService.store(client, new IndexFailure(mimeMailMessage.getMessageId(), mimeMailMessage.getPartition(), e));
                    }
                } catch (Exception e) {
                    logger.warn("Failed tp process message", e);
                    LogMessage processedMessage = new LogMessage(
                            LogLevel.Failure, messageId,
                            "Failed to process message " + e.getMessage());

                    messageMetaDataIndexService.store(client, processedMessage);
                    metaData.incrementFailures();
                }
                message = (PSTMessage) folder.getNextChild();

            }
        }

    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setMailBoxName(String mailBoxName) {
        this.mailBoxName = mailBoxName;
    }

    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setClient(String client) {
        this.client = client;
    }
}
