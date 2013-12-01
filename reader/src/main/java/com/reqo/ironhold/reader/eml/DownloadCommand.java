package com.reqo.ironhold.reader.eml;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.log.LogLevel;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.metadata.IMAPBatchMeta;
import com.reqo.ironhold.storage.model.search.IndexFailure;
import com.reqo.ironhold.web.domain.IndexedMailMessage;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Date;
import java.util.Properties;

/**
 * User: ilya
 * Date: 5/17/13
 * Time: 10:41 PM
 */
public class DownloadCommand implements IMAPFolder.ProtocolCommand {
    private static Logger logger = Logger.getLogger(DownloadCommand.class);

    private final IMimeMailMessageStorageService mimeMailMessageStorageService;
    private final MetaDataIndexService metaDataIndexService;
    private final MessageIndexService messageIndexService;
    private final MiscIndexService miscIndexService;
    private final String client;
    private final IMAPMessageSource source;
    private final IMAPBatchMeta metaData;

    /**
     * Index on server of first mail to fetch *
     */
    private final int start;

    /**
     * Index on server of last mail to fetch *
     */
    private final int end;
    private final boolean expunge;

    public DownloadCommand(IMimeMailMessageStorageService mimeMailMessageStorageService, MetaDataIndexService metaDataIndexService, MessageIndexService messageIndexService, MiscIndexService miscIndexService, String client, IMAPMessageSource source, IMAPBatchMeta metaData, boolean expunge, int start, int end) {

        this.mimeMailMessageStorageService = mimeMailMessageStorageService;
        this.metaDataIndexService = metaDataIndexService;
        this.messageIndexService = messageIndexService;
        this.miscIndexService = miscIndexService;
        this.client = client;
        this.source = source;
        this.metaData = metaData;
        this.expunge = expunge;
        this.start = start;
        this.end = end;
    }

    @Override
    public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
        Argument args = new Argument();
        args.writeString(Integer.toString(start) + ":" + Integer.toString(end));
        args.writeString("BODY[]");
        Response[] r = protocol.command("FETCH", args);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imap");
            props.setProperty("mail.mime.base64.ignoreerrors", "true");
            props.setProperty("mail.imap.partialfetch", "false");
            props.setProperty("mail.imaps.partialfetch", "false");
            Session session = Session.getInstance(props, null);

            FetchResponse fetch;
            BODY body;
            MimeMessage mm;
            ByteArrayInputStream is = null;

            // last response is only result summary: not contents
            for (int i = 0; i < r.length - 1; i++) {
                if (r[i] instanceof IMAPResponse) {
                    fetch = (FetchResponse) r[i];
                    body = (BODY) fetch.getItem(0);
                    is = body.getByteArrayInputStream();
                    try {
                        mm = new MimeMessage(session, is);
                        processMessage(i, mm);

                    } catch (Exception e1) {
                        throw new RuntimeException(e1);
                    }
                }
            }
        }
        // dispatch remaining untagged responses
        protocol.notifyResponseHandlers(r);
        protocol.handleResult(response);


        return "" + (r.length - 1);
    }

    private void processMessage(int currentMessageNumber, MimeMessage message) throws Exception {

        MimeMailMessage mailMessage = null;
        try {
            if (!message.getFlags().contains(Flags.Flag.DELETED)) {
                mailMessage = new MimeMailMessage();

                source.setLoadTimestamp(new Date());
                mailMessage.loadMimeMessage(message,
                        false);


                String messageId = mailMessage.getMessageId();

                if (mimeMailMessageStorageService.exists(client, mailMessage.getPartition(), mailMessage.getSubPartition(), messageId)) {
                    logger.warn("Found duplicate " + messageId);
                    metaData.incrementDuplicates();
                    metaDataIndexService.store(client, new LogMessage(LogLevel.Success, messageId, "Found duplicate message in " + source.getDescription()));

                } else {
                    long storedSize = mimeMailMessageStorageService.store(client, mailMessage.getPartition(), mailMessage.getSubPartition(), messageId, mailMessage.getRawContents(), CheckSumHelper.getCheckSum(mailMessage.getRawContents().getBytes()), true);
                    metaDataIndexService.store(client, source);

                    metaData.incrementBatchSize(storedSize);

                    metaDataIndexService.store(client, new LogMessage(LogLevel.Success, mailMessage.getMessageId(), "Stored journaled message from " + source.getDescription()));

                    logger.info("Stored journaled message["
                            + currentMessageNumber
                            + "] "
                            + mailMessage.getMessageId()
                            + " "
                            + FileUtils
                            .byteCountToDisplaySize(mailMessage
                                    .getSize()));

                    metaData.updateSizeStatistics(mailMessage
                            .getRawContents().length(), storedSize);

                    try {
                        messageIndexService.store(client, MimeMailMessage.toIndexedMailMessage(mailMessage, true));
                    } catch (Exception e) {
                        logger.error("Failed to index message " + mailMessage.getMessageId(), e);
                        metaDataIndexService.store(client, new IndexFailure(mailMessage.getMessageId(), mailMessage.getPartition(), e));
                    }

                }

                metaData.incrementAttachmentStatistics(mailMessage
                        .isHasAttachments());
                if (expunge) {
                    message.setFlag(Flags.Flag.DELETED, true);

                }
                metaData.incrementMessages();
            } else {
                logger.info("Skipping message that was marked deleted ["
                        + currentMessageNumber + "]");
            }
        } catch (AuthenticationFailedException | FolderClosedException | FolderNotFoundException | ReadOnlyFolderException |
                StoreClosedException e) {
            logger.error("Not able to process the mail reading.", e);
            System.exit(1);
        } catch (Exception e) {
            metaData.incrementFailures();
            if (mailMessage != null) {
                File f = new File(mailMessage.getMessageId() + ".eml");
                if (!f.exists()) {
                    FileUtils.writeStringToFile(f,
                            mailMessage.getRawContents());
                }

                logger.error("Failed to process message " + mailMessage.getMessageId(), e);
            } else {
                logger.error("Failed to process message", e);
            }
        }
    }
}