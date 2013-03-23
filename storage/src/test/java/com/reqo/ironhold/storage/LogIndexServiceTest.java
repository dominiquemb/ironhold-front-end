package com.reqo.ironhold.storage;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.LogMessageTestModel;
import com.reqo.ironhold.storage.model.log.LogMessage;
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
public class LogIndexServiceTest {

    private static final String INDEX_PREFIX = "unittest";
    private LogIndexService logIndexService;

    @ElasticsearchNode
    private static Node node;

    @ElasticsearchClient
    private static Client client;

    private IndexClient indexClient;

    @Before
    public void setUp() throws Exception {
        indexClient = new IndexClient(client);
        logIndexService = new LogIndexService(indexClient);
    }

    @After
    public void tearDown() {
        client.admin().indices().prepareDelete(INDEX_PREFIX);
    }


    @Test
    public void testLog() throws Exception {
        LogMessage inputMessage = LogMessageTestModel.generate();

        IndexResponse response = logIndexService.store(INDEX_PREFIX, inputMessage);

        Assert.assertTrue(indexClient.itemExists(INDEX_PREFIX + "." + LogIndexService.SUFFIX + "." + inputMessage.getPartition(), IndexedObjectType.LOG_MESSAGE, response.getId()));

        GetResponse response2 = indexClient.getById(INDEX_PREFIX + "." + LogIndexService.SUFFIX + "." + inputMessage.getPartition(), IndexedObjectType.LOG_MESSAGE, response.getId());

        Assert.assertEquals(inputMessage.serialize(), response2.getSourceAsString());

        indexClient.refresh(INDEX_PREFIX + "." + LogIndexService.SUFFIX + "." + inputMessage.getPartition());

        List<LogMessage> storedMessages = logIndexService
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

            logIndexService.store(INDEX_PREFIX, inputMessage);

            indexClient.refresh(INDEX_PREFIX + "." + LogIndexService.SUFFIX + "." + inputMessage.getPartition());

            inputMessages.add(inputMessage);
        }

        List<LogMessage> storedMessages = logIndexService
                .getLogMessages(INDEX_PREFIX, messageId);


        Assert.assertEquals(10, storedMessages.size());


    }

}
