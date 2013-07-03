package com.reqo.ironhold.storage.utils;

import com.reqo.ironhold.storage.LocalMimeMailMessageStorageService;
import com.reqo.ironhold.storage.security.LocalKeyStoreService;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

/**
 * User: ilya
 * Date: 4/12/13
 * Time: 3:04 PM
 */
public class VerifyDataStore {
    static {
        System.setProperty("jobname", VerifyDataStore.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(VerifyDataStore.class);

    public static String dataroot;
    public static String keystore;

    @Autowired
    private LocalKeyStoreService keyStoreService;

    @Autowired
    private LocalMimeMailMessageStorageService storageService;

    public VerifyDataStore() {

    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Options bean = new Options();
        CmdLineParser parser = new CmdLineParser(bean);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }

        try {
            dataroot = bean.getRootPath() + File.separator + "dataroot";
            keystore = bean.getRootPath() + File.separator + "keystore";

            ApplicationContext context = new ClassPathXmlApplicationContext("verifyISO.xml");
            VerifyDataStore util = context.getBean(VerifyDataStore.class);
            util.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.exit(1);
    }

    private void run() throws Exception {

        for (String client : storageService.getClients()) {
            logger.info("Verifying " + client);

            for (String partition : storageService.getPartitions(client)) {
                logger.info("Verifying " + client + "/" + partition);

                for (String subPartition : storageService.getSubPartitions(client, partition)) {
                    logger.info("Verifying " + client + "/" + partition + "/" + subPartition);

                    for (String fileName : storageService.getList(client, partition, subPartition)) {
                        storageService.get(client, partition, subPartition, fileName);
                    }

                }
            }
        }

        logger.info("Data Store verified succesfully");
    }


    static class Options {
        @Option(name = "-rootPath", usage = "path to root of data store", required = true)
        private String rootPath;

        String getRootPath() {
            return rootPath;
        }
    }

}
