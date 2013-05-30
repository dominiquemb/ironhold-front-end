package com.reqo.ironhold.storage.utils;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedMailMessage;
import com.reqo.ironhold.storage.model.user.RoleEnum;
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
public class MessageReconciliation {
    static {
        System.setProperty("jobname", MessageReconciliation.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(MessageReconciliation.class);

    @Autowired
    private MessageIndexService messageIndexService;

    @Autowired
    private IMimeMailMessageStorageService mimeMailMessageStorageService;

    public MessageReconciliation() {

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
        MessageReconciliation reconciliation = context.getBean(MessageReconciliation.class);
        reconciliation.run(bean.getClient(), bean.isAutofix());
        System.exit(1);
    }

    private void run(String client, boolean autofix) throws Exception {
        for (String partition : mimeMailMessageStorageService.getPartitions(client)) {
            logger.info("Checking " + partition);
            for (String subPartition : mimeMailMessageStorageService.getSubPartitions(client, partition)) {
                logger.info("Checking " + partition + "/" + subPartition);
                for (String messageId : mimeMailMessageStorageService.getList(client, partition, subPartition)) {
                    if (!messageIndexService.exists(client, partition, messageId)) {
                        logger.info("Message " + messageId + " is missing from the index");
                        if (autofix) {

                            String source = mimeMailMessageStorageService.get(client, partition, subPartition, messageId);
                            MimeMailMessage mimeMailMessage = new MimeMailMessage();
                            mimeMailMessage.loadMimeMessageFromSource(source);

                            IndexedMailMessage indexedMailMessage = new IndexedMailMessage(mimeMailMessage);

                            messageIndexService.store(client, indexedMailMessage, true);
                        }
                    }
                }
            }
        }

    }

    static class Options {
        @Option(name = "-client", usage = "client name", required = true)
        private String client;

        @Option(name = "-autofix", usage = "auto fix any reconciliation issues", required = false)
        private boolean autofix;

        boolean isAutofix() {
            return autofix;
        }

        public String getClient() {
            return client;
        }
    }

}
