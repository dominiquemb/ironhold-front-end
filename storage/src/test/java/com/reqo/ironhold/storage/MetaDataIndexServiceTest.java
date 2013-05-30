package com.reqo.ironhold.storage;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.AuditLogMessageTestModel;
import com.reqo.ironhold.storage.model.LogMessageTestModel;
import com.reqo.ironhold.storage.model.MessageSourceTestModel;
import com.reqo.ironhold.storage.model.PSTMessageTestModel;
import com.reqo.ironhold.storage.model.log.AuditLogMessage;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.message.source.MessageSource;
import com.reqo.ironhold.storage.model.message.source.PSTMessageSource;
import com.reqo.ironhold.storage.model.search.IndexFailure;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import junit.framework.Assert;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RunWith(ElasticsearchRunner.class)
@SuppressWarnings("unchecked")
public class MetaDataIndexServiceTest {

    private static final String INDEX_PREFIX = "unittest";
    private MetaDataIndexService metaDataIndexService;

    @ElasticsearchNode
    private static Node node;

    @ElasticsearchClient
    private static Client client;

    private IndexClient indexClient;
    private PSTMessageTestModel testModel;


    @Before
    public void setUp() throws Exception {
        indexClient = new IndexClient(client);
        metaDataIndexService = new MetaDataIndexService(indexClient);
        testModel = new PSTMessageTestModel("/attachments.pst");
    }

    @After
    public void tearDown() throws ExecutionException, InterruptedException {

        client.admin().indices().prepareDelete().execute().get();

    }


    @Test
    public void testAddPSTSource() throws Exception {

        PSTMessageSource pstSource = MessageSourceTestModel
                .generatePSTMessageSource();

        metaDataIndexService.store(INDEX_PREFIX, pstSource);


        indexClient.refresh(INDEX_PREFIX + "." + MetaDataIndexService.SUFFIX);

        List<MessageSource> sources = metaDataIndexService
                .getSources(INDEX_PREFIX, pstSource.getMessageId());

        Assert.assertEquals(1, sources.size());
        PSTMessageSource storedSource = (PSTMessageSource) sources.get(0);
        Assert.assertEquals(pstSource.serialize(),
                storedSource.serialize());

    }

    @Test
    public void testAddLargePSTSource() throws Exception {

        PSTMessageSource pstSource = MessageSourceTestModel
                .generatePSTMessageSource();
        pstSource.setSize(7723967488L);
        metaDataIndexService.store(INDEX_PREFIX, pstSource);


        indexClient.refresh(INDEX_PREFIX + "." + MetaDataIndexService.SUFFIX);

        List<MessageSource> sources = metaDataIndexService
                .getSources(INDEX_PREFIX, pstSource.getMessageId());

        Assert.assertEquals(1, sources.size());
        PSTMessageSource storedSource = (PSTMessageSource) sources.get(0);
        Assert.assertEquals(pstSource.serialize(),
                storedSource.serialize());
    }

    @Test
    public void testAddIMAPSource() throws Exception {
        IMAPMessageSource imapSource = MessageSourceTestModel
                .generateIMAPMessageSource();

        metaDataIndexService.store(INDEX_PREFIX, imapSource);


        indexClient.refresh(INDEX_PREFIX + "." + MetaDataIndexService.SUFFIX);

        List<MessageSource> sources = metaDataIndexService
                .getSources(INDEX_PREFIX, imapSource.getMessageId());

        Assert.assertEquals(1, sources.size());
        IMAPMessageSource storedSource = (IMAPMessageSource) sources.get(0);
        Assert.assertEquals(imapSource.serialize(),
                storedSource.serialize());


    }


    @Test
    public void testLog() throws Exception {
        LogMessage inputMessage = LogMessageTestModel.generate();

        metaDataIndexService.store(INDEX_PREFIX, inputMessage);

        indexClient.refresh(INDEX_PREFIX + "." + MetaDataIndexService.SUFFIX + "." + inputMessage.getPartition());

        List<LogMessage> storedMessages = metaDataIndexService
                .getLogMessages(INDEX_PREFIX, inputMessage.getMessageId());

        Assert.assertEquals(1, storedMessages.size());
        LogMessage storedMessage = storedMessages.get(0);
        Assert.assertEquals(inputMessage.serialize(),
                storedMessage.serialize());
    }

    @Test
    public void testGetLogMessages() throws Exception {
        String messageId = UUID.randomUUID().toString();
        List<LogMessage> inputMessages = new ArrayList<LogMessage>();
        for (int i = 0; i < 10; i++) {

            LogMessage inputMessage = LogMessageTestModel.generate();
            inputMessage.setMessageId(messageId);

            metaDataIndexService.store(INDEX_PREFIX, inputMessage);

            indexClient.refresh(INDEX_PREFIX + "." + MetaDataIndexService.SUFFIX + "." + inputMessage.getPartition());

            inputMessages.add(inputMessage);
        }

        List<LogMessage> storedMessages = metaDataIndexService
                .getLogMessages(INDEX_PREFIX, messageId);


        Assert.assertEquals(10, storedMessages.size());


    }

    @Test
    public void testAuditLog() throws Exception {
        AuditLogMessage inputMessage = AuditLogMessageTestModel.generate();

        metaDataIndexService.store(INDEX_PREFIX, inputMessage);

        indexClient.refresh(INDEX_PREFIX + "." + MetaDataIndexService.SUFFIX + "." + inputMessage.getPartition());
        List<AuditLogMessage> storedMessages = metaDataIndexService
                .getAuditLogMessages(INDEX_PREFIX, inputMessage.getMessageId());

        Assert.assertEquals(1, storedMessages.size());
        AuditLogMessage storedMessage = storedMessages.get(0);
        Assert.assertEquals(inputMessage.serialize(),
                storedMessage.serialize());
    }

    @Test
    public void testNoAuditLogFromSuperUser() throws Exception {
        AuditLogMessage inputMessage = AuditLogMessageTestModel.generate();
        metaDataIndexService.store(INDEX_PREFIX, inputMessage);

        AuditLogMessage inputMessage2 = AuditLogMessageTestModel.generate();
        inputMessage2.getLoginUser().setRolesBitMask(RoleEnum.SUPER_USER.getValue());
        metaDataIndexService.store(INDEX_PREFIX, inputMessage2);

        indexClient.refresh(INDEX_PREFIX + "." + MetaDataIndexService.SUFFIX + "." + inputMessage.getPartition());
        List<AuditLogMessage> storedMessages = metaDataIndexService
                .getAuditLogMessages(INDEX_PREFIX, inputMessage.getMessageId());

        Assert.assertEquals(1, storedMessages.size());
        AuditLogMessage storedMessage = storedMessages.get(0);
        Assert.assertEquals(inputMessage.serialize(),
                storedMessage.serialize());
    }

    @Test
    public void testGetAuditLogMessages() throws Exception {
        String messageId = UUID.randomUUID().toString();
        List<AuditLogMessage> inputMessages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {

            AuditLogMessage inputMessage = AuditLogMessageTestModel.generate();
            inputMessage.setMessageId(messageId);

            metaDataIndexService.store(INDEX_PREFIX, inputMessage);

            indexClient.refresh(INDEX_PREFIX + "." + MetaDataIndexService.SUFFIX + "." + inputMessage.getPartition());

            inputMessages.add(inputMessage);
        }

        List<AuditLogMessage> storedMessages = metaDataIndexService
                .getAuditLogMessages(INDEX_PREFIX, messageId);


        Assert.assertEquals(10, storedMessages.size());


    }

    @Test
    public void testIndexFailureStore() throws Exception {
        MimeMailMessage message = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());
        IndexFailure indexFailure = new IndexFailure(message.getMessageId(), message.getPartition(), new NullPointerException("test"));
        metaDataIndexService.store(INDEX_PREFIX, indexFailure);

        indexClient.refresh(INDEX_PREFIX + "." + MetaDataIndexService.SUFFIX + "." + message.getPartition());

        List<IndexFailure> indexFailures = metaDataIndexService.getIndexFailures(INDEX_PREFIX, 5);

        Assert.assertEquals(1, indexFailures.size());


        Assert.assertEquals(indexFailure.serialize(), indexFailures.get(0).serialize());
    }


}
