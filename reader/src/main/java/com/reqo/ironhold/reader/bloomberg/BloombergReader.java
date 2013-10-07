package com.reqo.ironhold.reader.bloomberg;

import com.reqo.ironhold.reader.bloomberg.model.bloomberg.converters.ConversationConverter;
import com.reqo.ironhold.reader.bloomberg.model.bloomberg.converters.MessageConverter;
import com.reqo.ironhold.reader.bloomberg.model.dscl.DisclaimerType;
import com.reqo.ironhold.reader.bloomberg.model.dscl.FileDumpType;
import com.reqo.ironhold.reader.bloomberg.model.ib.Conversation;
import com.reqo.ironhold.reader.bloomberg.model.msg.FileDump;
import com.reqo.ironhold.reader.bloomberg.model.msg.Message;
import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.log.LogLevel;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.BloombergSource;
import com.reqo.ironhold.storage.model.metadata.BloombergMeta;
import com.reqo.ironhold.storage.model.search.IndexFailure;
import com.reqo.ironhold.storage.model.search.IndexedMailMessage;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.*;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;

/**
 * User: ilya
 * Date: 10/6/13
 * Time: 4:04 PM
 */
public class BloombergReader {
    static {
        System.setProperty("jobname", BloombergReader.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(BloombergReader.class);

    @Autowired
    private IMimeMailMessageStorageService mimeMailMessageStorageService;

    @Autowired
    private MetaDataIndexService metaDataIndexService;

    @Autowired
    private MiscIndexService miscIndexService;

    @Autowired
    private MessageIndexService messageIndexService;

    private String hostname;
    private String client;
    private boolean encrypt;
    private int port;
    private String username;
    private String password;

    private BloombergSource source;
    private BloombergMeta metaData;
    private String dateSuffix;

    public BloombergReader() {
    }

    // Main Function for The readEmail Class
    public static void main(String args[]) {
        BloombergReaderOptions bean = new BloombergReaderOptions();
        CmdLineParser parser = new CmdLineParser(bean);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            logger.error(e);
            parser.printUsage(System.err);
            return;
        }
        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("readerContext.xml");
            BloombergReader reader = context.getBean(BloombergReader.class);

            reader.setHostname(bean.getHostname());
            reader.setPort(bean.getPort());
            reader.setUsername(bean.getUsername());
            reader.setPassword(bean.getPassword());
            reader.setClient(bean.getClient());
            reader.setEncrypt(bean.isEncrypt());

            long started = System.currentTimeMillis();
            int number = reader.process();
            long finished = System.currentTimeMillis();


            logger.info("Processed " + number
                    + " messages in " + (finished - started) + "ms");
        } catch (Exception e) {
            logger.error("Critical error detected, exiting", e);
            System.exit(1);
        }

    }

    private int process() throws Exception {
        metaData = new BloombergMeta(source, new Date());
        FileSystem fs = null;
        FileSystemOptions opts = new FileSystemOptions();
        FileSystemManager fsManager = VFS.getManager();
        FileObject path = fsManager.resolveFile("ftp://" + username + ":" + password + "@" + hostname +  ":" + port + "/daily_manifest_current.txt", opts);

        fs = path.getFileSystem();

        //prints Connection successfully established to /test/in

        InputStream is = path.getContent().getInputStream();

        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer);
        String lines = writer.toString();
        logger.info("Downloaded manifest file:");
        String msgFileName = null;
        String dsclFileName = null;
        String ibFileName = null;
        String attFileName = null;
        for (String line : lines.split("\n")) {
            logger.info(line);
            if (line.contains("msg")) {
                msgFileName = line.replace("\r","");
            } else if (line.contains("dscl")) {
                dsclFileName = line.replace("\r","");
            } else if (line.contains("ib")) {
                ibFileName = line.replace("\r","");
            } else if (line.contains("att")) {
                this.dateSuffix = line.replaceAll(".*\\.att\\.", "").replaceAll("\\.tar\\.gz\r", "");
                attFileName = line.replace("\r","");
            }
        }

        FileObject dsclFile = fsManager.resolveFile("ftp://" + username + ":" + password + "@" + hostname +  ":" + port + "/" + dsclFileName, opts);
        FileObject msgFile = fsManager.resolveFile("ftp://" + username + ":" + password + "@" + hostname +  ":" + port + "/" + msgFileName, opts);

        JAXBContext msgJaxbContext = JAXBContext.newInstance(FileDump.class);
        JAXBContext dsclJaxbContext = JAXBContext.newInstance(FileDumpType.class);

        Unmarshaller msgJaxbUnmarshaller = msgJaxbContext.createUnmarshaller();
        Unmarshaller dsclJaxbUnmarshaller = dsclJaxbContext.createUnmarshaller();


        FileDumpType disclaimers = (FileDumpType) dsclJaxbUnmarshaller.unmarshal(dsclFile.getContent().getInputStream());
        FileDump messages = (FileDump) msgJaxbUnmarshaller.unmarshal(msgFile.getContent().getInputStream());

        MessageConverter mc = new MessageConverter();

        int count=0;
        for (Message message : messages.getMessage()) {

            MimeMailMessage mimeMessage = mc.convert(message, getDisclaimer(disclaimers, message), "ftp://" + username + ":" + password + "@" + hostname +  ":" + port + "/" + attFileName);
            store(mimeMessage);

            count++;
            logger.info("Processed message " + count);
        }



        JAXBContext jaxbContext = JAXBContext.newInstance(com.reqo.ironhold.reader.bloomberg.model.ib.FileDump.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        FileObject ibFile = fsManager.resolveFile("ftp://" + username + ":" + password + "@" + hostname + ":" + port + "/" + ibFileName, opts);

        com.reqo.ironhold.reader.bloomberg.model.ib.FileDump conversations = (com.reqo.ironhold.reader.bloomberg.model.ib.FileDump) jaxbUnmarshaller.unmarshal(ibFile.getContent().getInputStream());
        ConversationConverter cc = new ConversationConverter();

        for (Conversation conversation : conversations.getConversation()) {
            MimeMailMessage mimeMessage = cc.convert(conversation, null, "ftp://" + username + ":" + password + "@" + hostname +  ":" + port + "/" + attFileName);
            store(mimeMessage);
            count++;
            logger.info("Processed conversation " + count);
        }

        metaData.setFinished(new Date());
        miscIndexService.store(client, metaData);

        return count;  //To change body of created methods use File | Settings | File Templates.
    }

    private void store(MimeMailMessage mailMessage) throws Exception {
        source = new BloombergSource();
        source.setFtpHostname(getHostname());
        source.setPort(getPort());
        source.setUsername(getUsername());
        source.setDate(dateSuffix);

        source.setLoadTimestamp(new Date());
        source.setPartition(mailMessage.getPartition());
        source.setMessageId(mailMessage.getMessageId());

        String messageId = mailMessage.getMessageId();

        if (mimeMailMessageStorageService.exists(client, mailMessage.getPartition(), mailMessage.getSubPartition(), messageId)) {
            logger.warn("Found duplicate " + messageId);
            metaData.incrementDuplicates();
            metaDataIndexService.store(client, new LogMessage(LogLevel.Success, messageId, "Found duplicate message in " + source.getDescription()));
            mimeMailMessageStorageService.archive(client, mailMessage.getPartition(), mailMessage.getSubPartition(), mailMessage.getMessageId());
        }

        long storedSize = mimeMailMessageStorageService.store(client, mailMessage.getPartition(), mailMessage.getSubPartition(), messageId, mailMessage.getRawContents(), CheckSumHelper.getCheckSum(mailMessage.getRawContents().getBytes()), this.encrypt);
        metaDataIndexService.store(client, source);

        metaData.incrementSize(storedSize);

        metaDataIndexService.store(client, new LogMessage(LogLevel.Success, mailMessage.getMessageId(), "Stored message from " + source.getDescription()));

        logger.info("Stored message "
                + mailMessage.getMessageId()
                + " "
                + FileUtils
                .byteCountToDisplaySize(mailMessage
                        .getSize()));

        metaData.updateSizeStatistics(mailMessage
                .getRawContents().length(), storedSize);

        try {
            IndexedMailMessage indexedMessage = messageIndexService.getById(client, mailMessage.getPartition(), mailMessage.getMessageId());
            if (indexedMessage == null) {
                indexedMessage = new IndexedMailMessage(mailMessage, true);
            }
            messageIndexService.store(client, indexedMessage, false);
        } catch (Exception e) {
            logger.error("Failed to index message " + mailMessage.getMessageId(), e);
            metaDataIndexService.store(client, new IndexFailure(mailMessage.getMessageId(), mailMessage.getPartition(), e));
        }


        metaData.incrementAttachmentStatistics(mailMessage
                .isHasAttachments());
        metaData.incrementMessages();
    }

    public static DisclaimerType getDisclaimer(FileDumpType disclaimers, Message message) {
        if (message.getDisclaimerReference() != null && message.getDisclaimerReference().getContent() != null && message.getDisclaimerReference().getContent().size() > 0) {
            String disclaimerReference = message.getDisclaimerReference().getContent().get(0);
            for (DisclaimerType disclaimer : disclaimers.getDisclaimer()) {
                if (disclaimer.getDisclaimerReference().equalsIgnoreCase(disclaimerReference)) {
                    return disclaimer;
                }

            }
        }
        return null;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }


    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

}
