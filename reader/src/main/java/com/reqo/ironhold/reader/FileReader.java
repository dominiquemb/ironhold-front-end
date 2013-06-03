package com.reqo.ironhold.reader;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
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

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;

public class FileReader {
    static {
        System.setProperty("jobname", FileReader.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(FileReader.class);

    @Autowired
    private IMimeMailMessageStorageService mimeMailMessageStorageService;

    @Autowired
    private MetaDataIndexService metaDataIndexService;

    @Autowired
    private MiscIndexService miscIndexService;

    @Autowired
    private MessageIndexService messageIndexService;


    private String emlFile;
    private String client;

    public FileReader(String client, String emlFile) throws IOException {
        this.client = client;
        this.emlFile = emlFile;
    }

    public void processMail() throws InterruptedException, MessagingException, FileNotFoundException {
        File file = new File(emlFile);
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(null, is);

        MimeMailMessage mailMessage = null;
        try {
            mailMessage = new MimeMailMessage();

            mailMessage.loadMimeMessage(mimeMessage, false);

            String messageId = mailMessage.getMessageId();

            boolean existsInStore = false;
            if (mimeMailMessageStorageService.exists(client, mailMessage.getPartition(), mailMessage.getSubPartition(), messageId)) {
                logger.warn("Found duplicate " + messageId);
                existsInStore = true;
            } else {
                mimeMailMessageStorageService.store(client, mailMessage.getPartition(), mailMessage.getSubPartition(), messageId, mailMessage.getRawContents(), CheckSumHelper.getCheckSum(mailMessage.getRawContents().getBytes()));

                logger.info("Stored journaled message "
                        + mailMessage.getMessageId()
                        + " "
                        + FileUtils
                        .byteCountToDisplaySize(mailMessage
                                .getSize()));
            }
            try {
                IndexedMailMessage indexedMessage = messageIndexService.getById(client, mailMessage.getPartition(), mailMessage.getMessageId());
                if (indexedMessage == null) {
                    indexedMessage = new IndexedMailMessage(mailMessage);
                }
                messageIndexService.store(client, indexedMessage, false);
            } catch (Exception e) {
                logger.error("Failed to index message " + mailMessage.getMessageId(), e);
                metaDataIndexService.store(client, new IndexFailure(mailMessage.getMessageId(), mailMessage.getPartition(), e));
            }

        } catch (Exception e) {
            if (mailMessage != null) {
                logger.info(mailMessage.getRawContents());
            }
            logger.error("Failed to process message", e);
        }

    }

    public String getEmlFile() {
        return emlFile;
    }

    public void setEmlFile(String emlFile) {
        this.emlFile = emlFile;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    // Main Function for The readEmail Class
    public static void main(String args[]) {
        FileReaderOptions bean = new FileReaderOptions();
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
            FileReader readMail = context.getBean(FileReader.class);

            readMail.setClient(bean.getClient());
            readMail.setEmlFile(bean.getEmlFile());

            try {
                long started = System.currentTimeMillis();
                readMail.processMail();
                long finished = System.currentTimeMillis();
                logger.info("Processed message in " + (finished - started) + "ms");

            } catch (InterruptedException e) {
                logger.warn("Got interrupted", e);
            }
        } catch (Exception e) {
            logger.error("Critical error detected, exiting", e);
            System.exit(1);
        }


    }

    public FileReader() {

    }
}
