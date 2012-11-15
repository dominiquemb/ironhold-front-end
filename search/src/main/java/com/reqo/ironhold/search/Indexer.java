package com.reqo.ironhold.search;

import com.reqo.ironhold.search.model.IndexedMailMessage;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.LogLevel;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MailMessage;
import org.apache.log4j.Logger;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.List;

public class Indexer {
    private static Logger logger = Logger.getLogger(Indexer.class);

    public static void main(String[] args) {
        IndexerOptions bean = new IndexerOptions();
        CmdLineParser parser = new CmdLineParser(bean);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }
        try {
            new Indexer(bean.getClient());
        } catch (Exception e) {
            logger.error("Critical error detected. Exiting.", e);
            System.exit(0);
        }

    }

    public Indexer(String client) throws Exception {
        IStorageService storageService = new MongoService(client, "indexer");
        IndexService indexService = new IndexService(client);

        while (true) {
            List<MailMessage> mailMessages = storageService.findUnindexedMessages(10);
            for (MailMessage mailMessage : mailMessages) {
                try {
                    indexService.store(new IndexedMailMessage(mailMessage));

                } catch (MapperParsingException e) {
                    logger.warn("Failed to index message " + mailMessage.getMessageId() + " with attachments, " +
                            "skiping attachments");

                    LogMessage logMessage = new LogMessage(LogLevel.Warning,
                            "Failed to index message with attachments, skiping attachments [" + e.getDetailedMessage
                                    () + "]", mailMessage.getMessageId());
                    storageService.log(logMessage);
                    mailMessage.getPstMessage().removeAttachments();
                    indexService.store(new IndexedMailMessage(mailMessage));
                }

                LogMessage logMessage = new LogMessage(LogLevel.Success, "Message indexed with " + mailMessage
                        .getPstMessage().getAttachments().length + " attachments", mailMessage.getMessageId());
                storageService.log(logMessage);

                storageService.markAsIndexed(mailMessage.getMessageId());
            }


            if (mailMessages.size() == 0) {
                Thread.sleep(10000);
            } else {
                logger.info("Indexed " + mailMessages.size() + " messages");
            }
        }
    }

}
