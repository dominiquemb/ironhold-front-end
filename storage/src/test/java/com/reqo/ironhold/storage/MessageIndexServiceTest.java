package com.reqo.ironhold.storage;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import com.gs.collections.api.list.ImmutableList;
import com.gs.collections.api.map.ImmutableMap;
import com.gs.collections.api.set.ImmutableSet;
import com.gs.collections.impl.block.factory.Predicates;
import com.pff.PSTMessage;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.model.LoginUserTestModel;
import com.reqo.ironhold.storage.model.PSTMessageTestModel;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.web.domain.*;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import org.apache.commons.io.FileUtils;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
    private Session session;

    @Before
    public void setUp() throws Exception {
        indexClient = new IndexClient(client);
        messageIndexService = new MessageIndexService(indexClient);
        testModel = new PSTMessageTestModel("/attachments.pst");
        superUser = LoginUserTestModel.generate(RoleEnum.SUPER_USER);
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.mime.base64.ignoreerrors", "true");
        props.setProperty("mail.imap.partialfetch", "false");
        props.setProperty("mail.imaps.partialfetch", "false");
        session = Session.getInstance(props, null);

    }

    @After
    public void tearDown() throws ExecutionException, InterruptedException {
        client.admin().indices().prepareDelete().execute().get();
    }

    @Test
    public void testStore() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = MimeMailMessage.toIndexedMailMessage(inputMessage, true);


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

        IndexedMailMessage indexedMailMessage = MimeMailMessage.toIndexedMailMessage(inputMessage, true);


        messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
        indexClient.refresh(INDEX_PREFIX);

        Thread.sleep(1000);

        String searchWord = inputMessage.getBody().split(" ")[0];
        CountSearchResponse result = messageIndexService.getMatchCount(INDEX_PREFIX, searchWord, superUser);

        Assert.assertEquals(1, result.getMatches());

        CountSearchResponse result2 = messageIndexService.getMatchCount(INDEX_PREFIX, "xxyyzz", superUser);

        Assert.assertEquals(0, result2.getMatches());

    }

    @Test
    public void testInvalidSearchTerm() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = MimeMailMessage.toIndexedMailMessage(inputMessage, true);


        messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
        indexClient.refresh(INDEX_PREFIX);

        CountSearchResponse result = messageIndexService.getMatchCount(INDEX_PREFIX, "xxyyzz(", superUser);

        Assert.assertEquals(-1, result.getMatches());

    }

    @Test
    public void testGetMatchCountWithBuilder() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = MimeMailMessage.toIndexedMailMessage(inputMessage, true);


        messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
        indexClient.refresh(INDEX_PREFIX);

        String searchWord = inputMessage.getBody().split(" ")[0];
        MessageSearchBuilder builder1 = messageIndexService.getNewBuilder(INDEX_PREFIX, superUser);
        builder1.withCriteria(searchWord);
        builder1.withResultsLimit(1, 10);
        builder1.withSort(IndexFieldEnum.DATE, SortOrder.ASC);

        Thread.sleep(1000);

        CountSearchResponse matchCount = messageIndexService.getMatchCount(builder1, superUser);

        Assert.assertEquals(1, matchCount.getMatches());

        MessageSearchBuilder builder2 = messageIndexService.getNewBuilder(INDEX_PREFIX, builder1, superUser);
        builder2.withCriteria("xxyyzz");

        CountSearchResponse notFound = messageIndexService.getMatchCount(builder2, superUser);

        Assert.assertEquals(0, notFound.getMatches());
    }

    @Test
    public void testSearchWithFacets() throws Exception {

        List<PSTMessage> inputMessages = testModel.generateOriginalPSTMessages();


        for (PSTMessage inputMessage : inputMessages) {
            MimeMailMessage message = MimeMailMessage.getMimeMailMessage(inputMessage);

            IndexedMailMessage toBeStored = MimeMailMessage.toIndexedMailMessage(message, true);


            messageIndexService.store(INDEX_PREFIX, toBeStored);
        }
        indexClient.refresh(INDEX_PREFIX);

        IndexedMailMessage indexedMailMessage = MimeMailMessage.toIndexedMailMessage(MimeMailMessage.getMimeMailMessage(inputMessages.get(0)), true);


        String searchWord = inputMessages.get(0).getSenderName().split(" ")[1];
        MessageSearchBuilder builder = messageIndexService.getNewBuilder(INDEX_PREFIX, superUser);
        builder.withResultsLimit(0, 10);
        builder.withSort(IndexFieldEnum.DATE, SortOrder.ASC);
        builder.withCriteria(searchWord);
        builder.withDateFacet().withFileExtFacet().withFromDomainFacet()
                .withFromFacet().withFullBody().withToFacet()
                .withToDomainFacet();
        MessageSearchResponse response = messageIndexService.search(builder);

        Assert.assertEquals(1, response.getMessages().size());

        assertFacet(response.getFacets(), FacetGroupName.FACET_YEAR.getValue(), 1, indexedMailMessage.getYear(), 1);
        assertFacet(response.getFacets(), FacetGroupName.FACET_TO_NAME.getValue(), 1, indexedMailMessage.getTo()[0].getName(), 1);
        assertFacet(response.getFacets(), FacetGroupName.FACET_FROM_NAME.getValue(), 1, indexedMailMessage.getSender().getName(), 1);
        assertFacet(response.getFacets(), FacetGroupName.FACET_TO_DOMAIN.getValue(), 1, indexedMailMessage.getTo()[0].getDomain(), 1);
        assertFacet(response.getFacets(), FacetGroupName.FACET_FROM_DOMAIN.getValue(), 1, indexedMailMessage.getSender().getDomain(), 1);
        assertFacet(response.getFacets(), FacetGroupName.FACET_FILEEXT.getValue(), 1, indexedMailMessage.getAttachments()[0].getFileExt(), 1);
    }

    private void assertFacet(ImmutableList<FacetGroup> facets, String facetGroupName, int totalSize, String thisLabel, int thisValue) {
        FacetGroup facetGroup = facets.detect(Predicates.attributeEqual(FacetGroup.GET_NAME, facetGroupName));
        Assert.assertEquals(totalSize, facetGroup.getValueMap().size());
        Assert.assertEquals(thisLabel, facetGroup.getValueMap().getFirst().getLabel());
        Assert.assertEquals(thisValue, facetGroup.getValueMap().getFirst().getValue());
    }


    @Test
    public void testGetTotalMessageCount() throws Exception {

        List<PSTMessage> messages = testModel.generateOriginalPSTMessages();
        for (PSTMessage message : messages) {
            MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(message);

            IndexedMailMessage indexedMailMessage = MimeMailMessage.toIndexedMailMessage(inputMessage, true);

            messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
            indexClient.refresh(INDEX_PREFIX);

        }

        Assert.assertEquals(messages.size(),
                messageIndexService.getTotalMessageCount(INDEX_PREFIX, superUser).getMatches());
    }


    @Test
    public void testStoreAndDelete() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = MimeMailMessage.toIndexedMailMessage(inputMessage, true);


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
            IndexedMailMessage indexedMailMessage = MimeMailMessage.toIndexedMailMessage(inputMessage, true);

            messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
            indexClient.refresh(INDEX_PREFIX);

        }

        Assert.assertEquals(messages.size(),
                messageIndexService.getTotalMessageCount(INDEX_PREFIX, superUser).getMatches());

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
            IndexedMailMessage indexedMailMessage = MimeMailMessage.toIndexedMailMessage(inputMessage, true);

            messageIndexService.store(INDEX_PREFIX, indexedMailMessage);
            indexClient.refresh(INDEX_PREFIX);

        }

        Assert.assertEquals(messages.size(),
                messageIndexService.getTotalMessageCount(INDEX_PREFIX, superUser).getMatches());

        IndicesGetAliasesResponse response = client.admin().indices().prepareGetAliases(INDEX_PREFIX).execute().get();
        Map<String, List<AliasMetaData>> aliases = response.getAliases();
        Assert.assertEquals(counter, aliases.keySet().size());
        for (int i = 2000; i < counter; i++) {
            Assert.assertTrue(aliases.containsKey(INDEX_PREFIX + "." + i));
        }

        messageIndexService.forceRefreshMappings(INDEX_PREFIX, true);
    }

    @Test
    public void testNoDate() throws Exception {
        File file = FileUtils.toFile(MessageIndexServiceTest.class
                .getResource("/testNoDate.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);


        IndexedMailMessage indexedMailMessage = MimeMailMessage.toIndexedMailMessage(mailMessage, true);


        Assert.assertEquals("unknown", indexedMailMessage.getYear());
        Assert.assertEquals("unknown", indexedMailMessage.getMonthDay());
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

    }

}
