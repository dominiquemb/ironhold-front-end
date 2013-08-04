package com.reqo.ironhold.demodata;

import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.model.log.LogLevel;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.search.IndexedMailMessage;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import com.reqo.ironhold.storage.thrift.MimeMailMessageStorageClient;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;

public class RandomEmailReader {
    static {
        System.setProperty("jobname", RandomEmailReader.class.getSimpleName());
    }

    @Autowired
    private RandomEmailGenerator randomEmailGenerator;


    @Autowired
    private MetaDataIndexService metaDataIndexService;

    @Autowired
    private MimeMailMessageStorageClient mimeMailMessageStorageService;

    @Autowired
    private MessageIndexService messageIndexService;

    public RandomEmailReader() {

    }

    public void process(String client, int number) throws Exception {
        for (int i = 0; i < number; i++) {
            MimeMailMessage mailMessage = new MimeMailMessage();
            mailMessage.loadMimeMessageFromSource(randomEmailGenerator.generate());

            final IMAPMessageSource source = new IMAPMessageSource();
            source.setImapPort(993);
            source.setUsername("leo");
            source.setImapSource("warandpeace.org");
            source.setProtocol("imaps");
            source.setLoadTimestamp(new Date());
            source.setMessageId(mailMessage.getMessageId());
            source.setPartition(mailMessage.getPartition());
            metaDataIndexService.store(client, source);

            mimeMailMessageStorageService.store(client, mailMessage.getPartition(), mailMessage.getSubPartition(), mailMessage.getMessageId(), mailMessage.getRawContents(), CheckSumHelper.getCheckSum(mailMessage.getRawContents().getBytes()));

            LogMessage logMessage = new LogMessage(LogLevel.Success,
                    mailMessage.getMessageId(),
                    "Stored journaled message from " + source.getProtocol()
                            + "://" + source.getImapSource() + ":"
                            + source.getImapPort());
            metaDataIndexService.store(client, logMessage);

            IndexedMailMessage indexedMailMessage = new IndexedMailMessage(mailMessage, true);
            messageIndexService.store(client, indexedMailMessage);
        }


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

        ApplicationContext context = new ClassPathXmlApplicationContext("demodataContext.xml");
        RandomEmailReader randomEmailReader = context.getBean(RandomEmailReader.class);
        randomEmailReader.process(bean.getClient(), bean.getNumber());


    }

}
