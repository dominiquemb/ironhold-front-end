package com.reqo.ironhold.importer.watcher;

import com.reqo.ironhold.importer.notification.EmailNotification;
import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;

public class InboundWatcher extends FileWatcher {
    static {
        System.setProperty("jobname", InboundWatcher.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(InboundWatcher.class);


    @Override
    protected void processFile(File dataFile, MD5CheckSum checksumFile) throws Exception {
        logger.info("Queuing valid file " + dataFile.toString());
        EmailNotification.sendSystemNotification("Queuing pst file: " + checksumFile.getDataFileName(), "File size: " + FileUtils.byteCountToDisplaySize(checksumFile.getDataFile().length()));
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
            InboundWatcher iw = new InboundWatcher();

            iw.setInputDirName(bean.getIn());
            iw.setOutputDirName(bean.getQueue());
            iw.setQuarantineDirName(bean.getQuarantine());
            iw.setClient(bean.getClient());

            iw.start();

        } catch (Exception e) {
            logger.error("Critical error detected. Exiting.", e);
            System.exit(0);
        }
    }


}
