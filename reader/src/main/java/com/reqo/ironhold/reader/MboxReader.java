package com.reqo.ironhold.reader;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.MimeMailMessage;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;

public class MboxReader {
    static {
        System.setProperty("jobname", MboxReader.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(MboxReader.class);
    private final IStorageService storageService;
    private String filePath;
    private int batchSize;
    private boolean expunge;

    public MboxReader(String filePath, String client, int batchSize,
                      boolean expunge) throws IOException {
        this.filePath = filePath;
        this.batchSize = batchSize;
        this.expunge = expunge;

        this.storageService = new MongoService(client, "MboxReader");

    }

    public int processMail() throws InterruptedException {
        Session session = null;
        Store store = null;
        Folder folder = null;
        int messageNumber = 0;


        try {

            System.setProperty("mail.store.protocol", "mstor");
            System.setProperty("mstor.mbox.metadataStrategy", "none");
            System.setProperty("mstor.mbox.cacheBuffers", "disabled");
            System.setProperty("mstor.mbox.bufferStrategy", "mapped");
            System.setProperty("mstor.metadata", "disabled");
            System.setProperty("mstor.mozillaCompatibility", "enabled");

            logger.info("Mbox Reader started");
            session = Session.getDefaultInstance(System.getProperties(), null);

            logger.info("Getting the session for accessing email.");
            store = session.getStore(new URLName("mstor:" + filePath));

            store.connect();

            logger.info("Connection established with Mbox file.");

            // Get a handle on the default folder
            folder = store.getDefaultFolder();

            logger.info("Getting the Inbox folder.");

//			// Retrieve the "Inbox"
//			folder = folder.getFolder("inbox");

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

                        mailMessage.loadMimeMessage((MimeMessage) message,
                                false);

                        messageId = mailMessage.getMessageId();

                        if (storageService.existsMimeMailMessage(messageId)) {
                            logger.warn("Found duplicate " + messageId);
                        } else {
                            long storedSize = storageService.store(mailMessage);


                            logger.info("Stored journaled message["
                                    + currentMessageNumber
                                    + "] "
                                    + mailMessage.getMessageId()
                                    + " "
                                    + FileUtils
                                    .byteCountToDisplaySize(mailMessage
                                            .getSize()));


                        }
                        if (expunge) {
                            message.setFlag(Flag.DELETED, true);

                        }
                    } else {
                        logger.info("Skipping message that was marked deleted ["
                                + messageNumber + "]");
                    }
                } catch (AuthenticationFailedException | FolderClosedException | FolderNotFoundException | ReadOnlyFolderException | StoreClosedException e) {
                    logger.error("Not able to process the mail reading.", e);
                    System.exit(1);
                } catch (Exception e) {
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
        MboxReaderOptions bean = new MboxReaderOptions();
        CmdLineParser parser = new CmdLineParser(bean);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            logger.error(e);
            parser.printUsage(System.err);
            return;
        }
        try {
            MboxReader readMail = new MboxReader(bean.getFilePath(), bean.getClient(), bean.getBatchSize(),
                    bean.getExpunge());

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
        } catch (IOException e) {
            logger.error("Critical error detected, exiting", e);
            System.exit(1);
        }

    }
}
