package com.reqo.ironhold.reader;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.security.CheckSumHelper;
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

    private String emlFile;
    private final String client;

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

            if (mimeMailMessageStorageService.exists(client, mailMessage.getPartition(), messageId)) {
                logger.warn("Found duplicate " + messageId);
            } else {

                mimeMailMessageStorageService.store(client, mailMessage.getPartition(), messageId, mailMessage.getRawContents(), CheckSumHelper.getCheckSum(mailMessage.getRawContents().getBytes()));
            }

        } catch (Exception e) {
            if (mailMessage != null) {
                logger.info(mailMessage.getRawContents());
            }
            logger.error("Failed to process message", e);
        }

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
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");


            FileReader readMail = new FileReader(bean.getClient(), bean.getEmlFile());

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
}
