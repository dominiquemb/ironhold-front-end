package com.reqo.ironhold.search;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import com.pff.PSTMessage;
import com.reqo.ironhold.search.es.PSTMessageTestModel;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedMailMessage;
import junit.framework.Assert;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(ElasticsearchRunner.class)
@SuppressWarnings("unchecked")
public class MessageIndexServiceTest {

    private static final String INDEX_PREFIX = "unittest";
    private MessageIndexService messageIndexService;

    @ElasticsearchNode
    private static Node node;

    @ElasticsearchClient
    private static Client client;

    private PSTMessageTestModel testModel;

    @Before
    public void setUp() throws Exception {
        messageIndexService = new MessageIndexService(client);
        testModel = new PSTMessageTestModel("/attachments.pst");
    }

    @After
    public void tearDown() {
        client.admin().indices().prepareDelete(INDEX_PREFIX);
    }

    @Test
    public void testStore() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                inputMessage);

        Assert.assertTrue(messageIndexService.store(INDEX_PREFIX, indexedMailMessage));
        messageIndexService.refresh(INDEX_PREFIX);

        String indexName = INDEX_PREFIX + "." + indexedMailMessage.getYear();
        IndicesExistsResponse exists = client.admin().indices()
                .prepareExists(indexName).execute().actionGet();

        Assert.assertTrue(exists.exists());

        GetResponse response = client
                .prepareGet(indexName, "mimeMessage",
                        indexedMailMessage.getMessageId()).execute()
                .actionGet();
        Assert.assertTrue(response.exists());
    }

    @Test
    public void testGetMatchCountWithString() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                inputMessage);

        Assert.assertTrue(messageIndexService.store(INDEX_PREFIX, indexedMailMessage));
        messageIndexService.refresh(INDEX_PREFIX);

        String searchWord = inputMessage.getBody().split(" ")[0];
        long matchCount = messageIndexService.getMatchCount(INDEX_PREFIX, searchWord);

        Assert.assertEquals(1, matchCount);

        long notFound = messageIndexService.getMatchCount(INDEX_PREFIX, "xxyyzz");

        Assert.assertEquals(0, notFound);

    }

    @Test
    public void testInvalidSearchTerm() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                inputMessage);

        Assert.assertTrue(messageIndexService.store(INDEX_PREFIX, indexedMailMessage));
        messageIndexService.refresh(INDEX_PREFIX);

        long invalidSearchTerm = messageIndexService.getMatchCount(INDEX_PREFIX, "xxyyzz(");

        Assert.assertEquals(-1, invalidSearchTerm);

    }

    @Test
    public void testGetMatchCountWithBuilder() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                inputMessage);

        Assert.assertTrue(messageIndexService.store(INDEX_PREFIX, indexedMailMessage));
        messageIndexService.refresh(INDEX_PREFIX);

        String searchWord = inputMessage.getBody().split(" ")[0];
        MessageSearchBuilder builder1 = messageIndexService.getNewBuilder(INDEX_PREFIX);
        builder1.withCriteria(searchWord);
        builder1.withResultsLimit(1, 10);
        builder1.withSort(IndexFieldEnum.DATE, SortOrder.ASC);

        SearchResponse matchCount = messageIndexService.getMatchCount(builder1);

        Assert.assertEquals(1, matchCount.getHits().getTotalHits());

        MessageSearchBuilder builder2 = messageIndexService.getNewBuilder(INDEX_PREFIX, builder1);
        builder2.withCriteria("xxyyzz");

        SearchResponse notFound = messageIndexService.getMatchCount(builder2);

        Assert.assertEquals(0, notFound.getHits().getTotalHits());
    }

    @Test
    public void testSearchWithFacets() throws Exception {

        List<PSTMessage> inputMessages = testModel.generateOriginalPSTMessages();


        for (PSTMessage inputMessage : inputMessages) {
            MimeMailMessage message = MimeMailMessage.getMimeMailMessage(inputMessage);

            IndexedMailMessage toBeStored = new IndexedMailMessage(
                    message);

            Assert.assertTrue(messageIndexService.store(INDEX_PREFIX, toBeStored));
        }
        messageIndexService.refresh(INDEX_PREFIX);

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(MimeMailMessage.getMimeMailMessage(inputMessages.get(0)));

        String searchWord = inputMessages.get(0).getSenderName().split(" ")[1];
        MessageSearchBuilder builder = messageIndexService.getNewBuilder(INDEX_PREFIX);
        builder.withResultsLimit(1, 10);
        builder.withSort(IndexFieldEnum.DATE, SortOrder.ASC);
        builder.withCriteria(searchWord);
        builder.withDateFacet().withFileExtFacet().withFromDomainFacet()
                .withFromFacet().withFullBody().withToFacet()
                .withToDomainFacet();
        SearchResponse response = messageIndexService.search(builder);

        Assert.assertEquals(1, response.getHits().getTotalHits());

        TermsFacet dateFacet = response.getFacets().facet(
                MessageSearchBuilder.FACET_YEAR);
        List<TermsFacet.Entry> years = (List<TermsFacet.Entry>) dateFacet
                .getEntries();
        Assert.assertEquals(1, years.size());
        Assert.assertEquals(indexedMailMessage.getYear(), years.get(0)
                .getTerm());
        Assert.assertEquals(1, years.get(0).getCount());

        TermsFacet toFacet = response.getFacets().facet(
                MessageSearchBuilder.FACET_TO_NAME);
        List<TermsFacet.Entry> toNames = (List<TermsFacet.Entry>) toFacet
                .getEntries();
        Assert.assertEquals(1, toNames.size());
        Assert.assertEquals(indexedMailMessage.getTo()[0].getName(), toNames
                .get(0).getTerm());
        Assert.assertEquals(1, toNames.get(0).getCount());

        TermsFacet fromFacet = response.getFacets().facet(
                MessageSearchBuilder.FACET_FROM_NAME);
        List<TermsFacet.Entry> fromNames = (List<TermsFacet.Entry>) fromFacet
                .getEntries();
        Assert.assertEquals(1, fromNames.size());
        Assert.assertEquals(indexedMailMessage.getSender().getName(), fromNames
                .get(0).getTerm());
        Assert.assertEquals(1, fromNames.get(0).getCount());

        TermsFacet toDomainFacet = response.getFacets().facet(
                MessageSearchBuilder.FACET_TO_DOMAIN);
        List<TermsFacet.Entry> toDomains = (List<TermsFacet.Entry>) toDomainFacet
                .getEntries();
        Assert.assertEquals(1, toDomains.size());
        Assert.assertEquals(indexedMailMessage.getTo()[0].getDomain(),
                toDomains.get(0).getTerm());
        Assert.assertEquals(1, toDomains.get(0).getCount());

        TermsFacet fromDomainFacet = response.getFacets().facet(
                MessageSearchBuilder.FACET_FROM_DOMAIN);
        List<TermsFacet.Entry> fromDomains = (List<TermsFacet.Entry>) fromDomainFacet
                .getEntries();
        Assert.assertEquals(1, fromDomains.size());
        Assert.assertEquals(indexedMailMessage.getSender().getDomain(),
                fromDomains.get(0).getTerm());
        Assert.assertEquals(1, fromDomains.get(0).getCount());

        TermsFacet fileExtFacet = response.getFacets().facet(
                MessageSearchBuilder.FACET_FILEEXT);
        List<TermsFacet.Entry> fileExts = (List<TermsFacet.Entry>) fileExtFacet
                .getEntries();
        Assert.assertEquals(1, fileExts.size());
        Assert.assertEquals(
                indexedMailMessage.getAttachments()[0].getFileExt(), fileExts
                .get(0).getTerm());
        Assert.assertEquals(1, fileExts.get(0).getCount());
    }

}
