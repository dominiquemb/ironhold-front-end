package com.reqo.ironhold.storage;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.PSTFileMetaTestModel;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;
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
public class MiscIndexServiceTest {

    private static final String INDEX_PREFIX = "unittest";
    private MiscIndexService miscIndexService;

    @ElasticsearchNode
    private static Node node;

    @ElasticsearchClient
    private static Client client;

    private IndexClient indexClient;

    @Before
    public void setUp() throws Exception {
        indexClient = new IndexClient(client);
        miscIndexService = new MiscIndexService(indexClient);
    }

    @After
    public void tearDown() {
        client.admin().indices().prepareDelete(INDEX_PREFIX);
    }


    @Test
    public void testStorePSTFileMeta() throws Exception {
        PSTFileMeta metaData = PSTFileMetaTestModel.generate();

        miscIndexService.store(INDEX_PREFIX, metaData);

        indexClient.refresh(INDEX_PREFIX + "." + MiscIndexService.SUFFIX);

        List<PSTFileMeta> pstFileMetaList = miscIndexService.getPSTFileMeta(INDEX_PREFIX, 0, 10);

        Assert.assertEquals(1, pstFileMetaList.size());
        for (PSTFileMeta pstFileMeta : pstFileMetaList) {
            Assert.assertEquals(metaData.serialize(), pstFileMeta.serialize());
        }

        Assert.assertTrue(miscIndexService.exists(INDEX_PREFIX, metaData));

        PSTFileMeta metaData2 = PSTFileMetaTestModel.generate();

        Assert.assertFalse(miscIndexService.exists(INDEX_PREFIX, metaData2));

    }


}
