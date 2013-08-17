package com.reqo.ironhold.storage.utils;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
    private MessageIndexService messageIndexService;

    @Autowired
    private IMimeMailMessageStorageService mimeMailMessageStorageService;

    @Autowired
    private MetaDataIndexService metaDataIndexService;

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
            System.err.println("Admin role: " + RoleEnum.SUPER_USER.getValue());
            return;
        }

        ApplicationContext context = new ClassPathXmlApplicationContext("utilities.xml");
        DecryptMessages reconciliation = context.getBean(DecryptMessages.class);
        reconciliation.run(bean.getClient());
        System.exit(1);
    }

    private void run(String client) throws Exception {
        int counter = 0;
        for (String partition : mimeMailMessageStorageService.getPartitions(client)) {
            logger.info("Checking " + partition);
            for (String subPartition : mimeMailMessageStorageService.getSubPartitions(client, partition)) {
                logger.info("Checking " + partition + "/" + subPartition);
                for (String messageId : mimeMailMessageStorageService.getList(client, partition, subPartition)) {

                    String messageSource = mimeMailMessageStorageService.get(client, partition, subPartition, messageId);
                    MimeMailMessage mimeMailMessage = new MimeMailMessage();
                    mimeMailMessage.loadMimeMessageFromSource(messageSource);
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
                }
            }
        }
    }


    static class Options {
        @Option(name = "-client", usage = "client name", required = true)
        private String client;


        public String getClient() {
            return client;
        }

    }

}
