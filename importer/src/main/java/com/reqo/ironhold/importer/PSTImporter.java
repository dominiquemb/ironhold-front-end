package com.reqo.ironhold.importer;

import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.log.LogLevel;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.PSTMessageSource;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;
import com.reqo.ironhold.storage.model.search.IndexFailure;
import com.reqo.ironhold.storage.model.search.IndexedMailMessage;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import com.reqo.ironhold.storage.thrift.MimeMailMessageStorageClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PSTImporter {

    private static final int MILLIS_IN_A_SEC = 1000;

    private static final int INFO_BATCH_SIZE = 100;

    private static Logger logger = Logger.getLogger(PSTImporter.class);

    @Autowired
    private MiscIndexService miscIndexService;

    @Autowired
    private MetaDataIndexService metaDataIndexService;

    @Autowired
    @Qualifier("mimeMailMessageStoreService")
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
    private boolean encrypt;

    private Set<String> ignoreAttachmentExtractSet = new HashSet<>();
    private Set<String> ignoreSet = new HashSet<>();

    public PSTImporter() throws Exception {
        this.hostname = InetAddress.getLocalHost().getHostName();
    }

    private boolean wasFileProcessedPreviously() throws Exception {
        PSTFileMeta existingMeta = miscIndexService.findExisting(client, this.metaData);
        return existingMeta != null && existingMeta.isCompleted();
    }

    public String processMessages() throws Exception {
        this.metaData = new PSTFileMeta(FilenameUtils.getBaseName(file.getName()), mailBoxName,
                originalFilePath, commentary, md5, hostname, file.length(), new Date());

        if (wasFileProcessedPreviously()) {
            throw new Exception("This file has been processed already");
        }

        miscIndexService.store(client, metaData);

        PSTFile pstFile = new PSTFile(file);
        try {
            String fileSizeDisplay = FileUtils.byteCountToDisplaySize(file
                    .length());

            LogMessage startedMessage = new LogMessage(LogLevel.Success,
                    file.toString(), "Started pst import: File: ["
                    + file.toString() + "] File size: ["
                    + fileSizeDisplay + "]");

            metaDataIndexService.store(client, startedMessage);

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


            metaDataIndexService.store(client, finishedMessage);
            metaData.setCompleted(true);
            miscIndexService.store(client, metaData);


            return messageString;
        } finally {
            pstFile.getFileHandle().close();
        }

    }

    private void processFolder(String folderPath, PSTFolder folder)
            throws Exception {
        folder.getSubFolderCount();

        LogMessage folderMessage = new LogMessage(LogLevel.Success,
                file.toString(), "Processing " + folderPath + " ["
                + folder.getContentCount() + " items]");
        metaDataIndexService.store(client, folderMessage);

        // go through the folders...
        if (folder.getSubFolderCount() > 0) {
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
                    if (!this.getIgnoreSet().contains(messageId.trim())) {
                        MimeMailMessage mimeMailMessage = MimeMailMessage.getMimeMailMessage(message);

                        if (mimeMailMessageStorageService.exists(client, mimeMailMessage.getPartition(), mimeMailMessage.getSubPartition(), mimeMailMessage.getMessageId())) {
                            logger.warn("Found duplicate " + messageId);
                            metaData.incrementDuplicates();


                            if (metaData.getDuplicates() % INFO_BATCH_SIZE == 0) {
                                logger.info("New Messages: "
                                        + metaData.getMessages() + " Duplicates: "
                                        + metaData.getDuplicates() + " Failures:"
                                        + metaData.getFailures());
                            }
                        } else {
                            logger.info("New message " + folderPath + "\\" + messageId);
                            if (metaData.getMessages() % INFO_BATCH_SIZE == 0) {
                                logger.info("New Messages: "
                                        + metaData.getMessages() + " Duplicates: "
                                        + metaData.getDuplicates() + " Failures:"
                                        + metaData.getFailures());
                            }
                            long storedSize = mimeMailMessageStorageService.store(client, mimeMailMessage.getPartition(), mimeMailMessage.getSubPartition(), mimeMailMessage.getMessageId(), mimeMailMessage.getRawContents(), CheckSumHelper.getCheckSum(mimeMailMessage.getRawContents().getBytes()), encrypt);
                            metaData.updateSizeStatistics(mimeMailMessage.getRawContents().length(), storedSize);
                        }

                        PSTMessageSource source = new PSTMessageSource(messageId,
                                FilenameUtils.getBaseName(file.toString()), folderPath, file.length(),
                                new Date(file.lastModified()), metaData.getId());

                        source.setPartition(mimeMailMessage.getPartition());
                        metaDataIndexService.store(client, source);
                        metaData.incrementObjectType(message.getClass().getSimpleName());

                        metaData.incrementAttachmentStatistics(mimeMailMessage.isHasAttachments());

                        metaData.incrementMessages();
                        LogMessage processedMessage = new LogMessage(
                                LogLevel.Success, messageId,
                                "Processed pst message");
                        metaDataIndexService.store(client, processedMessage);

                        try {
                            IndexedMailMessage indexedMessage = messageIndexService.getById(client, mimeMailMessage.getPartition(), mimeMailMessage.getMessageId());
                            if (indexedMessage == null) {

                                indexedMessage = new IndexedMailMessage(mimeMailMessage, !ignoreAttachmentExtractSet.contains(messageId.trim()));
                            }
                            indexedMessage.addSource(metaData.getId());
                            messageIndexService.store(client, indexedMessage, false);
                            if (ignoreAttachmentExtractSet.contains(messageId)) {
                                LogMessage warnMessage = new LogMessage(
                                        LogLevel.Warning, messageId,
                                        "Will not attempt to extract attachment text as this message id was blacklisted");

                                metaDataIndexService.store(client, warnMessage);

                            }
                        } catch (Exception e) {
                            logger.error("Failed to index message " + mimeMailMessage.getMessageId(), e);
                            metaDataIndexService.store(client, new IndexFailure(mimeMailMessage.getMessageId(), mimeMailMessage.getPartition(), e));
                        }
                    } else {
                        logger.warn("Skipping processing of message " + messageId);
                    }
                } catch (Exception e) {
                    if (e instanceof TTransportException) {
                        ((MimeMailMessageStorageClient) mimeMailMessageStorageService).reconnect();
                    }
                    logger.warn("Failed tp process message", e);
                    LogMessage processedMessage = new LogMessage(
                            LogLevel.Failure, messageId,
                            "Failed to process message " + e.getMessage());

                    metaDataIndexService.store(client, processedMessage);
                    metaData.incrementFailures();
                    debugLogMessage(message, folderPath);
                }
                message = (PSTMessage) folder.getNextChild();

            }
        }

    }

    private void debugLogMessage(PSTMessage message, String folderPath) throws PSTException, IOException {
        logger.info("folderPath:" + folderPath);
        logger.info("from:" + message.getSenderName() + ", " + message.getSenderEmailAddress());
        logger.info("subject:" + message.getSubject());
        logger.info("body:" + message.getBody());
        logger.info("getNumberOfRecipients:" + message.getNumberOfRecipients());
        logger.info(message.getClass().getName());
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

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public void initialize() throws Exception {
    }

    public void setIgnoreAttachmentExtractList(String ignoreAttachmentExtractList) {
        if (ignoreAttachmentExtractList != null) {
            try {
                String ignoreList = FileUtils.readFileToString(new File(ignoreAttachmentExtractList));
                for (String ignoreId : ignoreList.split("\n")) {
                    ignoreAttachmentExtractSet.add(ignoreId.trim());
                    logger.info("Ignoring " + ignoreId + " in attachment extraction");
                }
            } catch (Exception e) {
                logger.warn("Failed to fully process ignore Attachment Extraction file " + ignoreAttachmentExtractList, e);
            }
        }
    }


    public Set<String> getIgnoreAttachmentExtractSet() {
        return ignoreAttachmentExtractSet;
    }


    public void setIgnoreList(String ignoreListFile) {
        if (ignoreListFile != null) {
            try {
                String ignoreList = FileUtils.readFileToString(new File(ignoreListFile));
                for (String ignoreId : ignoreList.split("\n")) {
                    ignoreSet.add(ignoreId.trim());
                    logger.info("Ignoring " + ignoreId + "");
                }
            } catch (Exception e) {
                logger.warn("Failed to fully process ignore file " + ignoreListFile, e);
            }
        }
    }

    public Set<String> getIgnoreSet() {
        return ignoreSet;
    }
}
