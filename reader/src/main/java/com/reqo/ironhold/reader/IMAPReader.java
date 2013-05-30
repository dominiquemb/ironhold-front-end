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
    private int timeout;
    private IMAPClient imap;
    private IndexCommandListener indexCommandListener;

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

    public void initiateConnection() {


        if (protocol.equals("imaps")) {
            imap = new IMAPSClient("ssl", true); // implicit
            ((IMAPSClient) imap).setTrustManager(TrustManagerUtils.getValidateServerCertificateTrustManager());
        } else {
            imap = new IMAPClient();
        }
        imap.setDefaultTimeout(timeout);

        // suppress login details
        indexCommandListener = new IndexCommandListener();
        imap.addProtocolCommandListener(indexCommandListener);

        try {
            imap.connect(hostname, port);

            if (!imap.login(username, password)) {
                System.err.println("Could not login to server. Check password.");
                imap.disconnect();
                System.exit(3);
            }

            logger.info("Journal IMAP Reader started");
            imap.setSoTimeout(timeout);

            imap.capability();


            logger.info("Getting the Inbox folder.");


        } catch (Exception e) {
            logger.error("Not able to process the mail reading.", e);
            System.exit(1);
        }
    }

    public int processMail() throws InterruptedException {
        int count = 1;


        try {

            imap.select("inbox");

            if (expunge) {
                imap.expunge();
            }

            while (imap.fetch(Integer.toString(count), "(RFC822)") && count < batchSize && !indexCommandListener.nothingFetched()) {
                if (expunge) {
                    if (indexCommandListener.lastSuccess()) {
                        imap.store(Integer.toString(count), "+FLAGS.SILENT", "(\\Deleted)");
                    }

                }
                count++;
            }
            indexCommandListener.commit();

        } catch (Exception e) {
            logger.error("Not able to process the mail reading.", e);
            System.exit(1);
        }

        return count;
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
            readMail.setTimeout(bean.getTimeout());
            // Calling processMail Function to read from IMAP Account
            readMail.initiateConnection();
            try {
                while (true) {

                    long started = System.currentTimeMillis();
                    int number = readMail.processMail();
                    long finished = System.currentTimeMillis();
                    logger.info("Processed batch with " + (number - 1)
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


    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }


    private class IndexCommandListener implements ProtocolCommandListener {
        private final IMAPMessageSource source;
        private final IMAPBatchMeta metaData;
        private String currentCommand;
        private String currentResponse;
        private long started;
        private long finished;
        private boolean lastSuccess;

        public IndexCommandListener() {
            source = new IMAPMessageSource();
            source.setImapPort(port);
            source.setUsername(username);
            source.setImapSource(hostname);
            source.setImapPort(port);
            source.setProtocol(protocol);


            metaData = new IMAPBatchMeta(source, new Date());


        }

        public void commit() throws Exception {
            metaData.setFinished(new Date());
            miscIndexService.store(client, metaData);
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
        }

        @Override
        public void protocolReplyReceived(ProtocolCommandEvent event) {
            finished = System.currentTimeMillis();
            int firstLineMarker = event.getMessage().indexOf("\n");
            currentResponse = event.getMessage().substring(0, firstLineMarker - 1);
            logger.info("> " + currentResponse + " (" + (finished - started) + "ms)");
            if (currentResponse.contains("FETCH")) {

                String messageId = null;
                MimeMailMessage mailMessage = null;
                try {
                    mailMessage = new MimeMailMessage();
                    mailMessage.loadMimeMessageFromSource(event.getMessage().substring(firstLineMarker + 1));

                    source.setLoadTimestamp(new Date());

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

                        logger.info("Stored journaled message "
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
                    metaData.incrementMessages();

                    lastSuccess = true;
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
            }
        }

        public boolean lastSuccess() {
            return lastSuccess;
        }
    }
}
