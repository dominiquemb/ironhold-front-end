package com.reqo.ironhold.importer.watcher;

import com.reqo.ironhold.importer.PSTImporter;
import com.reqo.ironhold.importer.notification.EmailNotification;
import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

public class QueueWatcher extends FileWatcher {
    static {
        System.setProperty("jobname", QueueWatcher.class.getSimpleName());
    }


    private static Logger logger = Logger.getLogger(QueueWatcher.class);

    @Autowired
    private PSTImporter importer;


    @Override
    protected void processFile(File dataFile, MD5CheckSum checksumFile)
            throws Exception {
        logger.info("Processing data file " + dataFile.toString());

        importer.setMailBoxName(checksumFile.getMailBoxName());
        importer.setMd5(checksumFile.getCheckSum());
        importer.setCommentary(checksumFile.getCommentary());
        importer.setOriginalFilePath(checksumFile.getOriginalFilePath());
        importer.setFile(dataFile);
        importer.setClient(getClient());
        String details = importer.processMessages();

        EmailNotification.send("Finished processing pst file: " + checksumFile.getDataFileName(), details);

    }

    public static void main(String[] args) {
        WatcherOptions bean = new WatcherOptions();
        CmdLineParser parser = new CmdLineParser(bean);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }
        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("importerContext.xml");

            QueueWatcher qw = context.getBean(QueueWatcher.class);
            qw.setInputDirName(bean.getQueue());
            qw.setOutputDirName(bean.getOut());
            qw.setQuarantineDirName(bean.getQuarantine());
            qw.setClient(bean.getClient());

            qw.start();

        } catch (Exception e) {
            logger.error("Critical error detected. Exiting.", e);
            e.printStackTrace(System.err);
            System.exit(0);
        }
    }


}
