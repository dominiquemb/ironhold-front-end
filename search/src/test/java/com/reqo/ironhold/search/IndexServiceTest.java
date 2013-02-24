package com.reqo.ironhold.search;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
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
		testModel = new MailMessageTestModel("/data.pst");
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

		indexService.store(indexedMailMessage);
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
	public void testGetMatchCount() throws Exception {
		MailMessage inputMessage = testModel.generatePSTMessage();

		IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
				inputMessage);

		indexService.store(indexedMailMessage);

		RefreshResponse refresh = client.admin().indices()
				.prepareRefresh(INDEX_PREFIX).execute().actionGet();
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

		indexService.store(indexedMailMessage);
		indexService.refresh();
		
		long invalidSearchTerm = indexService.getMatchCount("xxyyzz(");

		Assert.assertEquals(-1, invalidSearchTerm);

	}

}
