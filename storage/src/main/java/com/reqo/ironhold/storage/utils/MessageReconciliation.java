package com.reqo.ironhold.storage.utils;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.MessageSource;
import com.reqo.ironhold.storage.model.message.source.PSTMessageSource;
import com.reqo.ironhold.web.domain.IndexedMailMessage;
import com.reqo.ironhold.storage.model.user.RoleEnum;
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
public class MessageReconciliation {
    static {
        System.setProperty("jobname", MessageReconciliation.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(MessageReconciliation.class);

    @Autowired
    private MessageIndexService messageIndexService;

    @Autowired
    private IMimeMailMessageStorageService mimeMailMessageStorageService;

    @Autowired
    private MetaDataIndexService metaDataIndexService;

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
        reconciliation.run(bean.getClient(), bean.isAutofix(), bean.isForce(), bean.getPartition(), bean.getSubPartition());
        System.exit(1);
    }

    private void run(String client, boolean autofix, boolean force, String partitionFocus, String subPartitionFocus) throws Exception {
        for (String partition : getPartitions(client, partitionFocus)) {
            logger.info("Checking " + partition);
            for (String subPartition : getSubPartitions(client, partition, subPartitionFocus)) {
                logger.info("Checking " + partition + "/" + subPartition);
                for (String messageId : mimeMailMessageStorageService.getList(client, partition, subPartition)) {

                    if (force || !messageIndexService.exists(client, partition, messageId)) {
                        if (!force) {
                            logger.info("Message " + messageId + " is missing from the index");
                        }
                        if (autofix) {

                            String messageSource = mimeMailMessageStorageService.get(client, partition, subPartition, messageId);
                            MimeMailMessage mimeMailMessage = new MimeMailMessage();
                            mimeMailMessage.loadMimeMessageFromSource(messageSource);

                            IndexedMailMessage indexedMailMessage = MimeMailMessage.toIndexedMailMessage(mimeMailMessage, true);

                            for (MessageSource existingSource : metaDataIndexService.getSources(client, messageId)) {
                                if (existingSource instanceof PSTMessageSource) {
                                    indexedMailMessage.addSource(((PSTMessageSource) existingSource).getPstFileMetaId());
                                }
                            }
                            messageIndexService.store(client, indexedMailMessage, false);
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

        @Option(name = "-autofix", usage = "auto fix any reconciliation issues", required = false)
        private boolean autofix;

        @Option(name = "-partition", usage = "partition to reconcile", required = false)
        private String partition;

        @Option(name = "-subPartition", usage = "subPartition to reconcile", required = false)
        private String subPartition;

        @Option(name = "-force", usage = "force reindex all messages", required = false)
        private boolean force;

        boolean isAutofix() {
            return autofix;
        }

        public String getClient() {
            return client;
        }

        String getPartition() {
            return partition;
        }

        String getSubPartition() {
            return subPartition;
        }

        boolean isForce() {
            return force;
        }
    }

}
