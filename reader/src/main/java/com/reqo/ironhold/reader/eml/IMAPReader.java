package com.reqo.ironhold.reader.eml;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.log.LogLevel;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.metadata.IMAPBatchMeta;
import com.reqo.ironhold.storage.model.search.IndexFailure;
import com.reqo.ironhold.storage.model.search.IndexedMailMessage;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.imap.IMAPClient;
import org.apache.commons.net.imap.IMAPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.mail.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class IMAPReader {
    static {
        System.setProperty("jobname", IMAPReader.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(IMAPReader.class);

    @Autowired
    private IMimeMailMessageStorageService mimeMailMessageStorageService;

    @Autowired
    private MetaDataIndexService metaDataIndexService;

    @Autowired
    private MiscIndexService miscIndexService;

    @Autowired
    private MessageIndexService messageIndexService;

    private String hostname;
    private int port;
    private String username;
    private String password;
    private String protocol;
    private int batchSize;
    private boolean expunge;
    private boolean encrypt;
    private String client;
    private int timeout;
    private IMAPClient imap;
    private IndexCommandListener indexCommandListener;
    private boolean testMode;
    private String folderMatch;
    private String folderNotMatch;

    public IMAPReader() {
    }

    public IMAPReader(String hostname, int port, String username,
                      String password, String protocol, String client, int batchSize,
                      boolean expunge, boolean encrypt, boolean testMode, String folderMatch, String folderNotMatch) throws IOException {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.protocol = protocol;
        this.batchSize = batchSize;
        this.expunge = expunge;
        this.client = client;
        this.encrypt = encrypt;
        this.testMode = testMode;
        this.folderMatch = folderMatch;
        this.folderNotMatch = folderNotMatch;
    }

    public void initiateConnection() throws Exception {

        if (protocol.equals("imaps")) {
            imap = new IMAPSClient("ssl", true); // implicit
            ((IMAPSClient) imap).setTrustManager(TrustManagerUtils.getValidateServerCertificateTrustManager());
        } else {
            imap = new IMAPClient();
        }
        try {
            imap.setDefaultTimeout(timeout);
            // suppress login details
            indexCommandListener = new IndexCommandListener(this.encrypt);
            imap.addProtocolCommandListener(indexCommandListener);

            imap.connect(hostname, port);
            imap.setSoTimeout(timeout);

            if (!imap.login(username, password)) {
                System.err.println("Could not login to server. Check password.");
                imap.disconnect();
                System.exit(3);
            }

            logger.info("IMAP Reader started");

            imap.capability();

            imap.setSoTimeout(timeout);


        } catch (Exception e) {
            logger.error("Not able to process the mail reading.", e);
            System.exit(1);
        }
    }

    public int processMail() throws InterruptedException {
        int totalCount = 0;

        try {

            imap.list("\"\"", "*");

            if (!indexCommandListener.lastSuccess()) {
                logger.error("Failed to get folders");
                return -1;
            }
            List<String> sortedList = new ArrayList<String>(indexCommandListener.getFolders());
            Collections.sort(sortedList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o2.compareTo(o1);
                }
            });
            for (String folder : sortedList) {
                if (folderMatch != null && !folder.equalsIgnoreCase(folderMatch)) {
                    logger.info("Skipping " + folder + " because it does not match " + folderMatch);
                    continue;
                }
                if (folderNotMatch != null && folder.equalsIgnoreCase(folderNotMatch)) {
                    logger.info("Skipping " + folder + " because it matches " + folderNotMatch);
                    continue;
                }
                int count = 1;
                logger.info("Processing " + folder);
                imap.select(folder);

                if (!indexCommandListener.lastSuccess()) {
                    logger.error("Failed to select folder " + folder);
                    return -1;
                }

                indexCommandListener.setCurrentFolder(folder);

                if (!testMode && expunge && indexCommandListener.getToBeProcessed() > 0) {
                    imap.expunge();
                    if (!indexCommandListener.lastSuccess()) {
                        logger.error("Failed to expunge folder " + folder);
                        return -1;
                    }

                    imap.select(folder);

                    if (!indexCommandListener.lastSuccess()) {
                        logger.error("Failed to select folder " + folder);
                        return -1;
                    }
                }

                //imap.status(folder, new String[]{"MESSAGES"});


                int toBeProcessed = batchSize;
                if (indexCommandListener.getToBeProcessed() < batchSize) {
                    toBeProcessed = indexCommandListener.getToBeProcessed();
                }
                logger.info("Can import " + toBeProcessed + " messages");
                if (toBeProcessed > 0) {
                    while (count <= toBeProcessed && imap.fetch(Integer.toString(count), "(RFC822)") && !indexCommandListener.nothingFetched()) {
                        if (!testMode && expunge) {
                            if (indexCommandListener.lastSuccess()) {
                                imap.store(Integer.toString(count), "+FLAGS.SILENT", "(\\Deleted)");
                            }

                        }
                        count++;
                        totalCount++;
                    }
                    indexCommandListener.commit();

                    if (count < batchSize && !testMode && expunge) {
                        imap.expunge();
                        if (!indexCommandListener.lastSuccess()) {
                            logger.error("Failed to expunge folder " + folder);
                            return -1;
                        }

                        if (!folder.equalsIgnoreCase("INBOX") && indexCommandListener.getToBeDeleted().contains(folder)) {
                            imap.select(folder);

                            if (!indexCommandListener.lastSuccess()) {
                                logger.error("Failed to select folder " + folder);
                                return -1;
                            }

                            if (indexCommandListener.getToBeProcessed() == 0) {
                                imap.delete(folder);
                                if (!indexCommandListener.lastSuccess()) {
                                    logger.warn("Failed to delete folder " + folder);
                                    indexCommandListener.getToBeDeleted().remove(folder);
                                } else {
                                    logger.info("Deleted folder " + folder);
                                }
                            }
                        }
                    }
                } else {
                    logger.info("Folder " + folder + " is empty");
                    if (!testMode && !folder.equalsIgnoreCase("INBOX") && indexCommandListener.getToBeDeleted().contains(folder)) {
                        imap.delete(folder);
                        if (!indexCommandListener.lastSuccess()) {
                            logger.warn("Failed to delete folder " + folder);
                            indexCommandListener.getToBeDeleted().remove(folder);
                        } else {
                            logger.info("Deleted folder " + folder);
                        }
                    }
                }

            }


        } catch (Exception e) {
            logger.error("Not able to process the mail reading.", e);
            return -1;
        }

        return totalCount;
    }

    // Main Function for The readEmail Class
    public static void main(String args[]) {
        ReaderOptions bean = new ReaderOptions();
        CmdLineParser parser = new CmdLineParser(bean);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            logger.error(e);
            parser.printUsage(System.err);
            return;
        }
        try {
            logger.info("IMAPReader will run in " + (bean.isTestMode() ? "TEST MODE" : "regular mode"));
            ApplicationContext context = new ClassPathXmlApplicationContext("readerContext.xml");
            IMAPReader readMail = context.getBean(IMAPReader.class);

            readMail.setHostname(bean.getHostname());
            readMail.setPort(bean.getPort());
            readMail.setUsername(bean.getUsername());
            readMail.setPassword(bean.getPassword());
            readMail.setProtocol(bean.getProtocol());
            readMail.setClient(bean.getClient());
            readMail.setBatchSize(bean.getBatchSize());
            readMail.setExpunge(bean.isExpunge());
            readMail.setTimeout(bean.getTimeout());
            readMail.setEncrypt(bean.isEncrypt());
            readMail.setTestMode(bean.isTestMode());
            readMail.setFolderMatch(bean.getFolderMatch());
            readMail.setFolderNotMatch(bean.getFolderNotMatch());

            // Calling processMail Function to read from IMAP Account
            readMail.initiateConnection();
            try {
                while (true) {

                    long started = System.currentTimeMillis();
                    int number = readMail.processMail();
                    long finished = System.currentTimeMillis();
                    logger.info("Processed batch with " + number
                            + " messages in " + (finished - started) + "ms");
                    if (number == -1) {
                        logger.warn("Lost connection, going to sleep");
                        Thread.sleep(60000);
                        readMail.initiateConnection();
                    } else if (number < bean.getBatchSize()) {
                        logger.info("Nothing more to process, going to sleep");
                        Thread.sleep(60000);

                    }
                }
            } catch (InterruptedException e) {
                logger.warn("Got interrupted", e);
            }
        } catch (Exception e) {
            logger.error("Critical error detected, exiting", e);
            System.exit(1);
        }

    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isExpunge() {
        return expunge;
    }

    public void setExpunge(boolean expunge) {
        this.expunge = expunge;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }


    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public void setFolderMatch(String folderMatch) {
        this.folderMatch = folderMatch;
    }

    public void setFolderNotMatch(String folderNotMatch) {
        this.folderNotMatch = folderNotMatch;
    }

    private class IndexCommandListener implements ProtocolCommandListener {
        private final IMAPMessageSource source;
        private final IMAPBatchMeta metaData;
        private final boolean encrypt;
        private String currentCommand;
        private String currentResponse;
        private long started;
        private long finished;
        private boolean lastSuccess;
        private int toBeProcessed;
        private Set<String> folders;
        private String currentFolder;
        private Set<String> toBeDeleted;

        public IndexCommandListener(boolean encrypt) {
            source = new IMAPMessageSource();
            source.setImapPort(port);
            source.setUsername(username);
            source.setImapSource(hostname);
            source.setImapPort(port);
            source.setProtocol(protocol);
            this.encrypt = encrypt;
            this.folders = new HashSet<>();
            this.toBeDeleted = new HashSet<>();

            metaData = new IMAPBatchMeta(source, new Date());
        }

        public void commit() throws Exception {
            metaData.setFinished(new Date());
            if (!testMode) {
                miscIndexService.store(client, metaData);
            }
        }

        public boolean nothingFetched() {
            return currentCommand.equals("FETCH") && !currentResponse.contains("FETCH");
        }

        @Override
        public void protocolCommandSent(ProtocolCommandEvent event) {
            logger.info("< " + event.getCommand());
            currentCommand = event.getCommand();
            started = System.currentTimeMillis();
            lastSuccess = false;
            toBeProcessed = 0;
        }

        @Override
        public void protocolReplyReceived(ProtocolCommandEvent event) {
            finished = System.currentTimeMillis();
            String[] lines = null;
            if (event.getMessage() == null || event.getMessage().isEmpty()) {
                return;
            }
            if (event.getMessage().contains("\n")) {
                lines = event.getMessage().split("\n");
            } else {
                lines = new String[]{event.getMessage()};
            }
            logger.info("> Response recieved (" + (finished - started) + "ms):\n" + lines[0]);
            for (String line : lines) {
                this.currentResponse = line;
                if (currentResponse != null && currentResponse.contains("FETCH")) {
                    String messageId = null;
                    MimeMailMessage mailMessage = null;
                    try {
                        mailMessage = new MimeMailMessage();
                        String rawMessage = event.getMessage();
                        long bytesToRead = 0;
                        while (rawMessage.startsWith("*")) {
                            String chunk = rawMessage.substring(0, rawMessage.indexOf("\n"));
                            if (chunk.contains(" FETCH ")) {

                                String bytesChunk = chunk.trim().split("\\{")[1];
                                bytesToRead = Long.parseLong(bytesChunk.replace("}", ""));
                            }
                            logger.info("> " + chunk);
                            rawMessage = rawMessage.substring(rawMessage.indexOf("\n") + 1);
                        }
                        if (bytesToRead > 0 && bytesToRead < Integer.MAX_VALUE) {
                            if (event.getMessage().length() - bytesToRead > 200) {
                                throw new IllegalArgumentException("Planning to read " + bytesToRead + " bytes, but there were " + event.getMessage().length() + " bytes in response so difference is greater than 200 ");
                            }
                            logger.info("Will read " + bytesToRead + " bytes, there were " + event.getMessage().length() + " bytes in response");
                            rawMessage = rawMessage.substring(0, (int) bytesToRead);
                        } else {
                            throw new IllegalArgumentException("Could not determine how many bytes to read");
                        }
                        mailMessage.loadMimeMessageFromSource(rawMessage);

                        if (!testMode) {

                            source.setLoadTimestamp(new Date());
                            source.setPartition(mailMessage.getPartition());
                            source.setFolder(indexCommandListener.getCurrentFolder());

                            messageId = mailMessage.getMessageId();

                            if (mimeMailMessageStorageService.exists(client, mailMessage.getPartition(), mailMessage.getSubPartition(), messageId)) {
                                logger.warn("Found duplicate " + messageId);
                                metaData.incrementDuplicates();
                                metaDataIndexService.store(client, new LogMessage(LogLevel.Success, messageId, "Found duplicate message in " + source.getDescription()));
                                mimeMailMessageStorageService.archive(client, mailMessage.getPartition(), mailMessage.getSubPartition(), mailMessage.getMessageId());
                            }

                            long storedSize = mimeMailMessageStorageService.store(client, mailMessage.getPartition(), mailMessage.getSubPartition(), messageId, mailMessage.getRawContents(), CheckSumHelper.getCheckSum(mailMessage.getRawContents().getBytes()), this.encrypt);
                            metaDataIndexService.store(client, source);

                            metaData.incrementBatchSize(storedSize);

                            metaDataIndexService.store(client, new LogMessage(LogLevel.Success, mailMessage.getMessageId(), "Stored message from " + source.getDescription()));

                            logger.info("Stored message "
                                    + mailMessage.getMessageId()
                                    + " "
                                    + FileUtils
                                    .byteCountToDisplaySize(mailMessage
                                            .getSize()));

                            metaData.updateSizeStatistics(mailMessage
                                    .getRawContents().length(), storedSize);

                            try {
                                IndexedMailMessage indexedMessage = messageIndexService.getById(client, mailMessage.getPartition(), mailMessage.getMessageId());
                                if (indexedMessage == null) {
                                    indexedMessage = new IndexedMailMessage(mailMessage, true);
                                }
                                messageIndexService.store(client, indexedMessage, false);
                            } catch (Exception e) {
                                logger.error("Failed to index message " + mailMessage.getMessageId(), e);
                                metaDataIndexService.store(client, new IndexFailure(mailMessage.getMessageId(), mailMessage.getPartition(), e));
                            }


                            metaData.incrementAttachmentStatistics(mailMessage
                                    .isHasAttachments());
                            metaData.incrementMessages();

                        } else {
                            logger.info("TEST MODE skipped message" + mailMessage.getMessageId()
                                    + " "
                                    + FileUtils
                                    .byteCountToDisplaySize(mailMessage
                                            .getSize()));
                        }
                        lastSuccess = true;
                        break;
                    } catch (AuthenticationFailedException | FolderClosedException | FolderNotFoundException | ReadOnlyFolderException | StoreClosedException e) {
                        logger.error("Not able to process the mail reading.", e);
                        System.exit(1);
                    } catch (Exception e) {
                        try {
                            metaData.incrementFailures();
                            if (mailMessage != null) {
                                File f = new File(mailMessage.getMessageId() + ".eml");
                                if (!f.exists()) {
                                    FileUtils.writeStringToFile(f,
                                            mailMessage.getRawContents());
                                }

                                logger.error("Failed to process message " + mailMessage.getMessageId(), e);
                            } else {
                                logger.error("Failed to process message", e);
                            }
                        } catch (Exception e1) {
                            logger.error("Critical error", e1);
                            System.exit(1);
                        }
                    }
                } else if (currentCommand != null && currentCommand.equals("SELECT") && currentResponse.contains("EXISTS")) {
                    this.toBeProcessed = Integer.parseInt(currentResponse.split(" ")[1]);
                } else if (currentCommand != null && currentCommand.equals("LIST") && currentResponse.contains("* LIST")) {
                    String folder = line.replaceAll("\\* LIST \\(.*\\) \"/\" ", "");
                    folder = folder.replaceAll("(\\r|\\n)", "");

                    if (!folders.contains(folder)) {// && !folder.toLowerCase().contains("outboox")) {
                        logger.info("Adding folder " + folder + " for processing");
                        folders.add(folder);

                        if (line.contains("HasNoChildren")) {
                            toBeDeleted.add(folder);
                        }
                    }
                }
            }

            lastSuccess = true;
        }

        public boolean lastSuccess() {
            return lastSuccess;
        }

        private int getToBeProcessed() {
            return toBeProcessed;
        }

        public Set<String> getFolders() {
            return folders;
        }

        private Set<String> getToBeDeleted() {
            return toBeDeleted;
        }

        private String getCurrentFolder() {
            return currentFolder;
        }

        private void setCurrentFolder(String currentFolder) {
            this.currentFolder = currentFolder;
        }
    }
}
