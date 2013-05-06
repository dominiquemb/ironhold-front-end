package com.reqo.ironhold.reader;

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
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

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
    private String client;

    public IMAPReader() {

    }

    public IMAPReader(String hostname, int port, String username,
                      String password, String protocol, String client, int batchSize,
                      boolean expunge) throws IOException {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.protocol = protocol;
        this.batchSize = batchSize;
        this.expunge = expunge;
        this.client = client;
    }

    public int processMail() throws InterruptedException {
        Session session = null;
        Store store = null;
        Folder folder = null;
        int messageNumber = 0;
        final IMAPMessageSource source = new IMAPMessageSource();

        source.setImapPort(port);
        source.setUsername(username);
        source.setImapSource(hostname);
        source.setImapPort(port);
        source.setProtocol(protocol);


        final IMAPBatchMeta metaData = new IMAPBatchMeta(source, new Date());

        try {
            logger.info("Journal IMAP Reader started");
            session = Session.getDefaultInstance(System.getProperties(), null);

            logger.info("Getting the session for accessing email.");
            store = session.getStore(protocol);

            store.connect(hostname, port, username, password);

            logger.info("Connection established with IMAP server.");

            // Get a handle on the default folder
            folder = store.getDefaultFolder();

            logger.info("Getting the Inbox folder.");

            // Retrieve the "Inbox"
            folder = folder.getFolder("inbox");

            // Reading the Email Index in Read / Write Mode
            folder.open(Folder.READ_WRITE);

            // Retrieve the messages
            final Message[] messages = folder.getMessages();

            logger.info("Found " + messages.length + " messages");

            // Loop over all of the messages

            for (messageNumber = 0; messageNumber < Math.min(batchSize,
                    messages.length); messageNumber++) {
                logger.info("Starting to process message " + messageNumber);
                MimeMailMessage mailMessage = null;
                String messageId = null;
                try {
                    final int currentMessageNumber = messageNumber;
                    // Retrieve the next message to be read
                    final Message message = messages[currentMessageNumber];
                    if (!message.getFlags().contains(Flag.DELETED)) {
                        mailMessage = new MimeMailMessage();

                        source.setLoadTimestamp(new Date());
                        mailMessage.loadMimeMessage((MimeMessage) message,
                                false);


                        messageId = mailMessage.getMessageId();

                        if (mimeMailMessageStorageService.exists(client, mailMessage.getPartition(), mailMessage.getSubPartition(), messageId)) {
                            logger.warn("Found duplicate " + messageId);
                            metaData.incrementDuplicates();
                            metaDataIndexService.store(client, new LogMessage(LogLevel.Success, messageId, "Found duplicate message in " + source.getDescription()));

                        } else {
                            long storedSize = mimeMailMessageStorageService.store(client, mailMessage.getPartition(), mailMessage.getSubPartition(), messageId, mailMessage.getRawContents(), CheckSumHelper.getCheckSum(mailMessage.getRawContents().getBytes()));
                            metaDataIndexService.store(client, source);

                            metaData.incrementBatchSize(storedSize);

                            metaDataIndexService.store(client, new LogMessage(LogLevel.Success, mailMessage.getMessageId(), "Stored journaled message from " + source.getDescription()));

                            logger.info("Stored journaled message["
                                    + currentMessageNumber
                                    + "] "
                                    + mailMessage.getMessageId()
                                    + " "
                                    + FileUtils
                                    .byteCountToDisplaySize(mailMessage
                                            .getSize()));

                            metaData.updateSizeStatistics(mailMessage
                                    .getRawContents().length(), storedSize);

                            try {
                                messageIndexService.store(client, new IndexedMailMessage(mailMessage));
                            } catch (Exception e) {
                                logger.error("Failed to index message " + mailMessage.getMessageId(), e);
                                metaDataIndexService.store(client, new IndexFailure(mailMessage.getMessageId(), mailMessage.getPartition(), e));
                            }

                        }

                        metaData.incrementAttachmentStatistics(mailMessage
                                .isHasAttachments());
                        if (expunge) {
                            message.setFlag(Flag.DELETED, true);

                        }
                        metaData.incrementMessages();
                    } else {
                        logger.info("Skipping message that was marked deleted ["
                                + messageNumber + "]");
                    }
                } catch (AuthenticationFailedException | FolderClosedException | FolderNotFoundException | ReadOnlyFolderException | StoreClosedException e) {
                    logger.error("Not able to process the mail reading.", e);
                    System.exit(1);
                } catch (Exception e) {
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
                }

            }

            metaData.setFinished(new Date());
            miscIndexService.store(client, metaData);
            // Close the folder
            folder.close(true);

            store.close();
        } catch (Exception e) {
            logger.error("Not able to process the mail reading.", e);
            System.exit(1);
        }

        return messageNumber;
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
            ApplicationContext context = new ClassPathXmlApplicationContext("readerContext.xml");
            IMAPReader readMail = context.getBean(IMAPReader.class);

            readMail.setHostname(bean.getHostname());
            readMail.setPort(bean.getPort());
            readMail.setUsername(bean.getUsername());
            readMail.setPassword(bean.getPassword());
            readMail.setProtocol(bean.getProtocol());
            readMail.setClient(bean.getClient());
            readMail.setBatchSize(bean.getBatchSize());
            readMail.setExpunge(bean.getExpunge());

            // Calling processMail Function to read from IMAP Account
            try {
                while (true) {

                    long started = System.currentTimeMillis();
                    int number = readMail.processMail();
                    long finished = System.currentTimeMillis();
                    logger.info("Processed batch with " + number
                            + " messages in " + (finished - started) + "ms");
                    if (number < bean.getBatchSize()) {

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

}
