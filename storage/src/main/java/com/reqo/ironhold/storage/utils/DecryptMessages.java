package com.reqo.ironhold.storage.utils;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.List;

/**
 * User: ilya
 * Date: 4/12/13
 * Time: 3:04 PM
 */
public class DecryptMessages {
    static {
        System.setProperty("jobname", DecryptMessages.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(DecryptMessages.class);

    @Autowired
    private IMimeMailMessageStorageService mimeMailMessageStorageService;

    public DecryptMessages() {

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

        ApplicationContext context = new ClassPathXmlApplicationContext("decrypt.xml");
        DecryptMessages reconciliation = context.getBean(DecryptMessages.class);
        reconciliation.run(bean.getClient(), bean.getPartition(), bean.getSubPartition());
        System.exit(1);
    }

    public void run(String client, String partitionFocus, String subPartitionFocus) throws Exception {
        int counter = 0;
        for (String partition : getPartitions(client, partitionFocus)) {
            logger.info("Checking " + partition);
            for (String subPartition : getSubPartitions(client, partition, subPartitionFocus)) {
                logger.info("Checking " + partition + "/" + subPartition);
                for (String messageId : mimeMailMessageStorageService.getList(client, partition, subPartition)) {
                    if (mimeMailMessageStorageService.isEncrypted(client, partition, subPartition, messageId)) {
                        try {
                            String messageSource = mimeMailMessageStorageService.get(client, partition, subPartition, messageId);
                            boolean archived = mimeMailMessageStorageService.archive(client, partition, subPartition, messageId);
                            if (archived) {
                                mimeMailMessageStorageService.store(client, partition, subPartition, messageId, messageSource, CheckSumHelper.getCheckSum(messageSource.getBytes()), false);
                                counter++;
                                if (counter % 100 == 0) {
                                    logger.info("Decrypted " + counter + " messages");
                                }
                            } else {
                                logger.warn("Failed to archive and decrypt message: " + messageId);
                            }
                        } catch (Exception e) {
                            logger.warn("Failed to archive and decrypt message: " + messageId, e);
                        }
                    }
                }
            }
        }
    }

    private List<String> getPartitions(String client, String partitionFocus) throws Exception {
        if (partitionFocus == null || partitionFocus.isEmpty()) {
            return mimeMailMessageStorageService.getPartitions(client);
        } else {
            return Arrays.asList(new String[]{partitionFocus});
        }
    }

    private List<String> getSubPartitions(String client, String partition, String subPartitionFocus) throws Exception {
        if (subPartitionFocus == null || subPartitionFocus.isEmpty()) {
            return mimeMailMessageStorageService.getSubPartitions(client, partition);
        } else {
            return Arrays.asList(new String[]{subPartitionFocus});
        }
    }

    static class Options {
        @Option(name = "-client", usage = "client name", required = true)
        private String client;

        @Option(name = "-partition", usage = "partition to reconcile", required = false)
        private String partition;

        @Option(name = "-subPartition", usage = "subPartition to reconcile", required = false)
        private String subPartition;

        public String getClient() {
            return client;
        }

        public String getPartition() {
            return partition;
        }

        public String getSubPartition() {
            return subPartition;
        }
    }

}
