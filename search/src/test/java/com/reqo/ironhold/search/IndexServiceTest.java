package com.reqo.ironhold.search;

import java.util.List;

import junit.framework.Assert;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchClient;
import com.github.tlrx.elasticsearch.test.annotations.ElasticsearchNode;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import com.reqo.ironhold.search.model.IndexedMailMessage;
import com.reqo.ironhold.search.model.MailMessageTestModel;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.Recipient;

@RunWith(ElasticsearchRunner.class)
public class IndexServiceTest {

	private static final String INDEX_PREFIX = "unittest";
	private IndexService indexService;

	@ElasticsearchNode
	private static Node node;

	@ElasticsearchClient
	private static Client client;

	private MailMessageTestModel testModel;

	@Before
	public void setUp() throws Exception {
		indexService = new IndexService(INDEX_PREFIX, client);
		testModel = new MailMessageTestModel("/attachments.pst");
	}

	@After
	public void tearDown() {
		client.admin().indices().prepareDelete(INDEX_PREFIX);
	}

	@Test
	public void testStore() throws Exception {
		MailMessage inputMessage = testModel.generatePSTMessage();

		IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
				inputMessage);

		Assert.assertTrue(indexService.store(indexedMailMessage));
		indexService.refresh();

		String indexName = INDEX_PREFIX + "." + indexedMailMessage.getYear();
		IndicesExistsResponse exists = client.admin().indices()
				.prepareExists(indexName).execute().actionGet();

		Assert.assertTrue(exists.exists());

		GetResponse response = client
				.prepareGet(indexName, "message",
						indexedMailMessage.getMessageId()).execute()
				.actionGet();
		Assert.assertTrue(response.exists());
	}

	@Test
	public void testGetMatchCountWithString() throws Exception {
		MailMessage inputMessage = testModel.generatePSTMessage();

		IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
				inputMessage);

		Assert.assertTrue(indexService.store(indexedMailMessage));
		indexService.refresh();

		String searchWord = inputMessage.getPstMessage().getBody().split(" ")[0];
		long matchCount = indexService.getMatchCount(searchWord);

		Assert.assertEquals(1, matchCount);

		long notFound = indexService.getMatchCount("xxyyzz");

		Assert.assertEquals(0, notFound);

	}

	@Test
	public void testInvalidSearchTerm() throws Exception {
		MailMessage inputMessage = testModel.generatePSTMessage();

		IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
				inputMessage);

		Assert.assertTrue(indexService.store(indexedMailMessage));
		indexService.refresh();

		long invalidSearchTerm = indexService.getMatchCount("xxyyzz(");

		Assert.assertEquals(-1, invalidSearchTerm);

	}

	@Test
	public void testGetMatchCountWithBuilder() throws Exception {
		MailMessage inputMessage = testModel.generatePSTMessage();

		IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
				inputMessage);

		Assert.assertTrue(indexService.store(indexedMailMessage));
		indexService.refresh();

		String searchWord = inputMessage.getPstMessage().getBody().split(" ")[0];
		MessageSearchBuilder builder1 = indexService.getNewBuilder();
		builder1.withCriteria(searchWord);
		builder1.withResultsLimit(1, 10);
		builder1.withSort(IndexFieldEnum.DATE, SortOrder.ASC);

		SearchResponse matchCount = indexService.getMatchCount(builder1);

		Assert.assertEquals(1, matchCount.getHits().getTotalHits());

		MessageSearchBuilder builder2 = indexService.getNewBuilder(builder1);
		builder2.withCriteria("xxyyzz");

		SearchResponse notFound = indexService.getMatchCount(builder2);

		Assert.assertEquals(0, notFound.getHits().getTotalHits());
	}

	@Test
	public void testSearchWithFacets() throws Exception {
		List<MailMessage> inputMessages = testModel.generatePSTMessages();

		
		for (MailMessage inputMessage : inputMessages) {
			IndexedMailMessage toBeStored = new IndexedMailMessage(
					inputMessage);

			Assert.assertTrue(indexService.store(toBeStored));
		}
		indexService.refresh();
		
		IndexedMailMessage indexedMailMessage = new IndexedMailMessage(inputMessages.get(0));

		String searchWord = inputMessages.get(0).getPstMessage().getSenderName().split(" ")[1];
		MessageSearchBuilder builder = indexService.getNewBuilder();
		builder.withResultsLimit(1, 10);
		builder.withSort(IndexFieldEnum.DATE, SortOrder.ASC);
		builder.withCriteria(searchWord);
		builder.withDateFacet().withFileExtFacet().withFromDomainFacet()
				.withFromFacet().withFullBody().withToFacet()
				.withToDomainFacet();
		SearchResponse response = indexService.search(builder);

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
