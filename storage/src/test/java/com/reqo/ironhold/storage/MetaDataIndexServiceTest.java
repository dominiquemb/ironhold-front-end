package com.reqo.ironhold.storage;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.MessageSourceTestModel;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.message.source.MessageSource;
import com.reqo.ironhold.storage.model.message.source.PSTMessageSource;
import junit.framework.Assert;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

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

    @Before
    public void setUp() throws Exception {
        indexClient = new IndexClient(client);
        metaDataIndexService = new MetaDataIndexService(indexClient);
    }

    @After
    public void tearDown() {
        client.admin().indices().prepareDelete(INDEX_PREFIX);
    }


    @Test
    public void testAddPSTSource() throws Exception {

        PSTMessageSource pstSource = MessageSourceTestModel
                .generatePSTMessageSource();

        metaDataIndexService.store(INDEX_PREFIX, pstSource);


        indexClient.refresh(INDEX_PREFIX + "." + MetaDataIndexService.SUFFIX + "." + pstSource.getPartition());

        List<MessageSource> sources = metaDataIndexService
                .getSources(INDEX_PREFIX, pstSource.getMessageId());

        Assert.assertEquals(1, sources.size());
        PSTMessageSource storedSource = (PSTMessageSource)sources.get(0);
        Assert.assertEquals(pstSource.serialize(),
                storedSource.serialize());

    }

    @Test
    public void testAddIMAPSource() throws Exception {
        IMAPMessageSource imapSource = MessageSourceTestModel
                .generateIMAPMessageSource();

        metaDataIndexService.store(INDEX_PREFIX, imapSource);


        indexClient.refresh(INDEX_PREFIX + "." + MetaDataIndexService.SUFFIX + "." + imapSource.getPartition());

        List<MessageSource> sources = metaDataIndexService
                .getSources(INDEX_PREFIX, imapSource.getMessageId());

        Assert.assertEquals(1, sources.size());
        IMAPMessageSource storedSource = (IMAPMessageSource)sources.get(0);
        Assert.assertEquals(imapSource.serialize(),
                storedSource.serialize());


    }


}
