package com.reqo.ironhold.storage;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.LogMessageTestModel;
import com.reqo.ironhold.storage.model.MessageSourceTestModel;
import com.reqo.ironhold.storage.model.PSTMessageTestModel;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.message.source.MessageSource;
import com.reqo.ironhold.storage.model.message.source.PSTMessageSource;
import com.reqo.ironhold.storage.model.search.IndexFailure;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import junit.framework.Assert;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(ElasticsearchRunner.class)
@SuppressWarnings("unchecked")
public class MessageMetaDataIndexServiceTest {

    private static final String INDEX_PREFIX = "unittest";
    private MessageMetaDataIndexService messageMetaDataIndexService;

    @ElasticsearchNode
    private static Node node;

    @ElasticsearchClient
    private static Client client;

    private IndexClient indexClient;
    private PSTMessageTestModel testModel;

    @Before
    public void setUp() throws Exception {
        indexClient = new IndexClient(client);
        messageMetaDataIndexService = new MessageMetaDataIndexService(indexClient);
        testModel = new PSTMessageTestModel("/attachments.pst");
    }

    @After
    public void tearDown() {
        client.admin().indices().prepareDelete(INDEX_PREFIX);
    }


    @Test
    public void testAddPSTSource() throws Exception {

        PSTMessageSource pstSource = MessageSourceTestModel
                .generatePSTMessageSource();

        messageMetaDataIndexService.store(INDEX_PREFIX, pstSource);


        indexClient.refresh(INDEX_PREFIX + "." + MessageMetaDataIndexService.SUFFIX + "." + pstSource.getPartition());

        List<MessageSource> sources = messageMetaDataIndexService
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

        messageMetaDataIndexService.store(INDEX_PREFIX, imapSource);


        indexClient.refresh(INDEX_PREFIX + "." + MessageMetaDataIndexService.SUFFIX + "." + imapSource.getPartition());

        List<MessageSource> sources = messageMetaDataIndexService
                .getSources(INDEX_PREFIX, imapSource.getMessageId());

        Assert.assertEquals(1, sources.size());
        IMAPMessageSource storedSource = (IMAPMessageSource) sources.get(0);
        Assert.assertEquals(imapSource.serialize(),
                storedSource.serialize());


    }


    @Test
    public void testLog() throws Exception {
        LogMessage inputMessage = LogMessageTestModel.generate();

        IndexResponse response = messageMetaDataIndexService.store(INDEX_PREFIX, inputMessage);

        Assert.assertTrue(indexClient.itemExists(INDEX_PREFIX + "." + MessageMetaDataIndexService.SUFFIX + "." + inputMessage.getPartition(), IndexedObjectType.LOG_MESSAGE, response.getId()));

        GetResponse response2 = indexClient.getById(INDEX_PREFIX + "." + MessageMetaDataIndexService.SUFFIX + "." + inputMessage.getPartition(), IndexedObjectType.LOG_MESSAGE, response.getId());

        Assert.assertEquals(inputMessage.serialize(), response2.getSourceAsString());

        indexClient.refresh(INDEX_PREFIX + "." + MessageMetaDataIndexService.SUFFIX + "." + inputMessage.getPartition());

        List<LogMessage> storedMessages = messageMetaDataIndexService
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

            messageMetaDataIndexService.store(INDEX_PREFIX, inputMessage);

            indexClient.refresh(INDEX_PREFIX + "." + MessageMetaDataIndexService.SUFFIX + "." + inputMessage.getPartition());

            inputMessages.add(inputMessage);
        }

        List<LogMessage> storedMessages = messageMetaDataIndexService
                .getLogMessages(INDEX_PREFIX, messageId);


        Assert.assertEquals(10, storedMessages.size());


    }

    @Test
    public void testIndexFailureStore() throws Exception {
        MimeMailMessage message = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());
        IndexFailure indexFailure = new IndexFailure(message.getMessageId(), message.getPartition(), new NullPointerException("test"));
        messageMetaDataIndexService.store(INDEX_PREFIX, indexFailure);

        indexClient.refresh(INDEX_PREFIX + "." + MessageMetaDataIndexService.SUFFIX + "." + message.getPartition());

        List<IndexFailure> indexFailures = messageMetaDataIndexService.getIndexFailures(INDEX_PREFIX, 5);

        Assert.assertEquals(1, indexFailures.size());


        Assert.assertEquals(indexFailure.serialize(), indexFailures.get(0).serialize());
    }


}
