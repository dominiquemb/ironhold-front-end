package com.reqo.ironhold.search;

import com.reqo.ironhold.search.model.IndexedMailMessage;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.LogLevel;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MailMessage;
import org.apache.log4j.Logger;
import org.elasticsearch.index.mapper.MapperParsingException;

import java.util.List;

public class Indexer {
    private static Logger logger = Logger.getLogger(Indexer.class);

    public static void main(String[] args) {
        try {
            new Indexer();
        } catch (Exception e) {
            logger.error("Critical error detected. Exiting.", e);
            System.exit(0);
        }
    }

    public Indexer() throws Exception {
        IStorageService storageService = new MongoService("reqo", "indexer");
        IndexService indexService = new IndexService("reqo");

        while (true) {
            List<MailMessage> mailMessages = storageService
                    .findUnindexedMessages(10);
            for (MailMessage mailMessage : mailMessages) {
                try {
                    indexService.store(new IndexedMailMessage(mailMessage));

                } catch (MapperParsingException e) {
                    logger.warn("Failed to index message "
                            + mailMessage.getMessageId()
                            + " with attachments, skiping attachments");

                    LogMessage logMessage = new LogMessage(LogLevel.Warning,
                            "Failed to index message with attachments, skiping attachments ["
                                    + e.getDetailedMessage() + "]",
                            mailMessage.getMessageId());
                    storageService.log(logMessage);
                    mailMessage.getPstMessage().removeAttachments();
                    indexService.store(new IndexedMailMessage(mailMessage));
                }

                LogMessage logMessage = new LogMessage(
                        LogLevel.Success,
                        "Message indexed with "
                                + mailMessage.getPstMessage().getAttachments().length
                                + " attachments", mailMessage.getMessageId());
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
