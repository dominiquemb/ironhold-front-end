package com.reqo.ironhold.reader;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.metadata.IMAPBatchMeta;
import com.sun.mail.imap.IMAPFolder;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.mail.*;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

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
            Properties props = System.getProperties();
            props.setProperty("mail.imaps.ssl.trust", hostname);
            props.setProperty("mail.imaps.connectionpoolsize", "10");

            logger.info("Journal IMAP Reader started");
            session = Session.getDefaultInstance(props, null);

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

            efficientGetContents((IMAPFolder) folder, folder.getMessages(), source, metaData);

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

    public int efficientGetContents(IMAPFolder inbox, Message[] messages, IMAPMessageSource source, IMAPBatchMeta metaData)
            throws MessagingException, IOException {
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.FLAGS);
        fp.add(FetchProfile.Item.ENVELOPE);
        inbox.fetch(messages, fp);
        int index = 0;
        int nbMessages = messages.length;
        final int maxDoc = 5000;
        final long maxSize = 100000000; // 100Mo

        // Message numbers limit to fetch
        int start;
        int end;

        while (index < nbMessages) {
            start = messages[index].getMessageNumber();
            int docs = 0;
            int totalSize = 0;
            boolean noskip = true; // There are no jumps in the message numbers
            // list
            boolean notend = true;
            // Until we reach one of the limits
            while (docs < maxDoc && totalSize < maxSize && noskip && notend) {
                docs++;
                totalSize += messages[index].getSize();
                index++;
                if (notend = (index < nbMessages)) {
                    noskip = (messages[index - 1].getMessageNumber() + 1 == messages[index]
                            .getMessageNumber());
                }
            }

            end = messages[index - 1].getMessageNumber();
            inbox.doCommand(new DownloadCommand(mimeMailMessageStorageService, metaDataIndexService, messageIndexService, miscIndexService, client, source, metaData, expunge, start, end));

            System.out.println("Fetching contents for " + start + ":" + end);
            System.out.println("Size fetched = " + (totalSize / 1000000)
                    + " Mo");

        }

        return nbMessages;
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
