package com.reqo.ironhold.importer.watcher;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;

import java.io.File;

public class InboundWatcher extends FileWatcher {
    private static Logger logger = Logger.getLogger(InboundWatcher.class);

    public InboundWatcher(String inputDirName, String outputDirName, String quarantineDirName,  String client) throws Exception {
        super(inputDirName, outputDirName, quarantineDirName, client);
    }


    @Override
    protected void processFile(File dataFile, MD5CheckSum checksumFile) throws Exception {
        logger.info("Queuing valid file " + dataFile.toString());
        send("Queuing pst file: " + checksumFile.getDataFileName(),"File size: " + FileUtils.byteCountToDisplaySize(checksumFile.getDataFile().length()));
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
            new InboundWatcher(bean.getIn(), bean.getQueue(), bean.getQuarantine(), bean.getClient());
        } catch (Exception e) {
            logger.error("Critical error detected. Exiting.", e);
            System.exit(0);
        }
    }
}
