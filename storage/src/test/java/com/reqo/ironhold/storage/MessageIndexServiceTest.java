package com.reqo.ironhold.storage;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import com.pff.PSTMessage;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.model.LoginUserTestModel;
import com.reqo.ironhold.storage.model.PSTMessageTestModel;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import junit.framework.Assert;
import org.elasticsearch.action.admin.indices.alias.get.IndicesGetAliasesResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
    private IndexClient indexClient;
    private LoginUser superUser;

    @Before
    public void setUp() throws Exception {
        indexClient = new IndexClient(client);
        messageIndexService = new MessageIndexService(indexClient);
        testModel = new PSTMessageTestModel("/attachments.pst");
        superUser = LoginUserTestModel.generate(RoleEnum.SUPER_USER);


    }

    @After
    public void tearDown() throws ExecutionException, InterruptedException {
        client.admin().indices().prepareDelete().execute().get();
    }

    @Test
    public void testStore() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                inputMessage, true);

        messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
        indexClient.refresh(INDEX_PREFIX);

        String indexName = INDEX_PREFIX + "." + indexedMailMessage.getYear();
        IndicesExistsResponse exists = client.admin().indices()
                .prepareExists(indexName).execute().actionGet();

        Assert.assertTrue(exists.isExists());

        GetResponse response = client
                .prepareGet(indexName, "mimeMessage",
                        indexedMailMessage.getMessageId()).execute()
                .actionGet();
        Assert.assertTrue(response.isExists());

        Assert.assertEquals(indexedMailMessage.serialize(), response.getSourceAsString());
    }

    @Test
    public void testGetMatchCountWithString() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                inputMessage, true);

        messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
        indexClient.refresh(INDEX_PREFIX);

        Thread.sleep(1000);

        String searchWord = inputMessage.getBody().split(" ")[0];
        long matchCount = messageIndexService.getMatchCount(INDEX_PREFIX, searchWord, superUser);

        Assert.assertEquals(1, matchCount);

        long notFound = messageIndexService.getMatchCount(INDEX_PREFIX, "xxyyzz", superUser);

        Assert.assertEquals(0, notFound);

    }

    @Test
    public void testInvalidSearchTerm() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                inputMessage, true);

        messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
        indexClient.refresh(INDEX_PREFIX);

        long invalidSearchTerm = messageIndexService.getMatchCount(INDEX_PREFIX, "xxyyzz(", superUser);

        Assert.assertEquals(-1, invalidSearchTerm);

    }

    @Test
    public void testGetMatchCountWithBuilder() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                inputMessage, true);

        messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
        indexClient.refresh(INDEX_PREFIX);

        String searchWord = inputMessage.getBody().split(" ")[0];
        MessageSearchBuilder builder1 = messageIndexService.getNewBuilder(INDEX_PREFIX, superUser);
        builder1.withCriteria(searchWord);
        builder1.withResultsLimit(1, 10);
        builder1.withSort(IndexFieldEnum.DATE, SortOrder.ASC);

        Thread.sleep(1000);

        SearchResponse matchCount = messageIndexService.getMatchCount(builder1, superUser);

        Assert.assertEquals(1, matchCount.getHits().getTotalHits());

        MessageSearchBuilder builder2 = messageIndexService.getNewBuilder(INDEX_PREFIX, builder1, superUser);
        builder2.withCriteria("xxyyzz");

        SearchResponse notFound = messageIndexService.getMatchCount(builder2, superUser);

        Assert.assertEquals(0, notFound.getHits().getTotalHits());
    }

    @Test
    public void testSearchWithFacets() throws Exception {

        List<PSTMessage> inputMessages = testModel.generateOriginalPSTMessages();


        for (PSTMessage inputMessage : inputMessages) {
            MimeMailMessage message = MimeMailMessage.getMimeMailMessage(inputMessage);

            IndexedMailMessage toBeStored = new IndexedMailMessage(
                    message, true);

            messageIndexService.store(INDEX_PREFIX, toBeStored);
        }
        indexClient.refresh(INDEX_PREFIX);

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(MimeMailMessage.getMimeMailMessage(inputMessages.get(0)), true);

        String searchWord = inputMessages.get(0).getSenderName().split(" ")[1];
        MessageSearchBuilder builder = messageIndexService.getNewBuilder(INDEX_PREFIX, superUser);
        builder.withResultsLimit(1, 10);
        builder.withSort(IndexFieldEnum.DATE, SortOrder.ASC);
        builder.withCriteria(searchWord);
        builder.withDateFacet().withFileExtFacet().withFromDomainFacet()
                .withFromFacet().withFullBody().withToFacet()
                .withToDomainFacet();
        SearchResponse response = messageIndexService.search(builder, superUser);

        Assert.assertEquals(1, response.getHits().getTotalHits());

        TermsFacet dateFacet = response.getFacets().facet(
                MessageSearchBuilder.FACET_YEAR);
        List<TermsFacet.Entry> years = (List<TermsFacet.Entry>) dateFacet
                .getEntries();
        Assert.assertEquals(1, years.size());
        Assert.assertEquals(indexedMailMessage.getYear(), years.get(0)
                .getTerm().toString());
        Assert.assertEquals(1, years.get(0).getCount());

        TermsFacet toFacet = response.getFacets().facet(
                MessageSearchBuilder.FACET_TO_NAME);
        List<TermsFacet.Entry> toNames = (List<TermsFacet.Entry>) toFacet
                .getEntries();
        Assert.assertEquals(1, toNames.size());
        Assert.assertEquals(indexedMailMessage.getTo()[0].getName(), toNames
                .get(0).getTerm().toString());
        Assert.assertEquals(1, toNames.get(0).getCount());

        TermsFacet fromFacet = response.getFacets().facet(
                MessageSearchBuilder.FACET_FROM_NAME);
        List<TermsFacet.Entry> fromNames = (List<TermsFacet.Entry>) fromFacet
                .getEntries();
        Assert.assertEquals(1, fromNames.size());
        Assert.assertEquals(indexedMailMessage.getSender().getName(), fromNames
                .get(0).getTerm().toString());
        Assert.assertEquals(1, fromNames.get(0).getCount());

        TermsFacet toDomainFacet = response.getFacets().facet(
                MessageSearchBuilder.FACET_TO_DOMAIN);
        List<TermsFacet.Entry> toDomains = (List<TermsFacet.Entry>) toDomainFacet
                .getEntries();
        Assert.assertEquals(1, toDomains.size());
        Assert.assertEquals(indexedMailMessage.getTo()[0].getDomain(),
                toDomains.get(0).getTerm().toString());
        Assert.assertEquals(1, toDomains.get(0).getCount());

        TermsFacet fromDomainFacet = response.getFacets().facet(
                MessageSearchBuilder.FACET_FROM_DOMAIN);
        List<TermsFacet.Entry> fromDomains = (List<TermsFacet.Entry>) fromDomainFacet
                .getEntries();
        Assert.assertEquals(1, fromDomains.size());
        Assert.assertEquals(indexedMailMessage.getSender().getDomain(),
                fromDomains.get(0).getTerm().toString());
        Assert.assertEquals(1, fromDomains.get(0).getCount());

        TermsFacet fileExtFacet = response.getFacets().facet(
                MessageSearchBuilder.FACET_FILEEXT);
        List<TermsFacet.Entry> fileExts = (List<TermsFacet.Entry>) fileExtFacet
                .getEntries();
        Assert.assertEquals(1, fileExts.size());
        Assert.assertEquals(
                indexedMailMessage.getAttachments()[0].getFileExt(), fileExts
                .get(0).getTerm().toString());
        Assert.assertEquals(1, fileExts.get(0).getCount());
    }


    @Test
    public void testGetTotalMessageCount() throws Exception {

        List<PSTMessage> messages = testModel.generateOriginalPSTMessages();
        for (PSTMessage message : messages) {
            MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(message);

            IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                    inputMessage, true);
            messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
            indexClient.refresh(INDEX_PREFIX);

        }

        Assert.assertEquals(messages.size(),
                messageIndexService.getTotalMessageCount(INDEX_PREFIX, superUser));
    }


    @Test
    public void testStoreAndDelete() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                inputMessage, true);

        messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
        indexClient.refresh(INDEX_PREFIX);

        String indexName = INDEX_PREFIX + "." + indexedMailMessage.getYear();
        IndicesExistsResponse exists = client.admin().indices()
                .prepareExists(indexName).execute().actionGet();

        Assert.assertTrue(exists.isExists());

        GetResponse response = client
                .prepareGet(indexName, IndexedObjectType.MIME_MESSAGE.getValue(),
                        indexedMailMessage.getMessageId()).execute()
                .actionGet();
        Assert.assertTrue(response.isExists());

        Assert.assertEquals(indexedMailMessage.serialize(), response.getSourceAsString());
        indexClient.refresh(indexName);

        messageIndexService.deleteByField(INDEX_PREFIX, indexedMailMessage.getPartition(), IndexedObjectType.MIME_MESSAGE, "messageId", indexedMailMessage.getMessageId());

        Assert.assertFalse(messageIndexService.exists(INDEX_PREFIX, indexedMailMessage.getPartition(), indexedMailMessage.getMessageId()));

    }


    @Test
    public void testMultipleYears() throws Exception {

        List<PSTMessage> messages = testModel.generateOriginalPSTMessages();
        int counter = 0;
        int year = 2000;
        for (PSTMessage message : messages) {
            MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(message);

            inputMessage.setMessageDate(new DateTime(year + counter, 1, 1, 1, 1).toDate());
            counter++;
            IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                    inputMessage, true);
            messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
            indexClient.refresh(INDEX_PREFIX);

        }

        Assert.assertEquals(messages.size(),
                messageIndexService.getTotalMessageCount(INDEX_PREFIX, superUser));

        IndicesGetAliasesResponse response = client.admin().indices().prepareGetAliases(INDEX_PREFIX).execute().get();
        Map<String, List<AliasMetaData>> aliases = response.getAliases();
        Assert.assertEquals(counter, aliases.keySet().size());
        for (int i = 2000; i < counter; i++) {
            Assert.assertTrue(aliases.containsKey(INDEX_PREFIX + "." + i));
        }
    }


    @Test
    public void testRefreshMappings() throws Exception {

        List<PSTMessage> messages = testModel.generateOriginalPSTMessages();
        int counter = 0;
        int year = 2000;
        for (PSTMessage message : messages) {
            MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(message);

            inputMessage.setMessageDate(new DateTime(year + counter, 1, 1, 1, 1).toDate());
            counter++;
            IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                    inputMessage, true);
            messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
            indexClient.refresh(INDEX_PREFIX);

        }

        Assert.assertEquals(messages.size(),
                messageIndexService.getTotalMessageCount(INDEX_PREFIX, superUser));

        IndicesGetAliasesResponse response = client.admin().indices().prepareGetAliases(INDEX_PREFIX).execute().get();
        Map<String, List<AliasMetaData>> aliases = response.getAliases();
        Assert.assertEquals(counter, aliases.keySet().size());
        for (int i = 2000; i < counter; i++) {
            Assert.assertTrue(aliases.containsKey(INDEX_PREFIX + "." + i));
        }

        messageIndexService.forceRefreshMappings(INDEX_PREFIX, true);
    }
}
