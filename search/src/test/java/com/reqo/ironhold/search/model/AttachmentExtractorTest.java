package com.reqo.ironhold.search.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.mail.internet.MimeMessage;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MailMessageTestModel;
import com.reqo.ironhold.storage.model.MimeMailMessage;

public class AttachmentExtractorTest {
	private MailMessageTestModel testModel;

	@Before
	public void setUp() throws Exception {
		testModel = new MailMessageTestModel("/attachments.pst");
	}

	@Test
	public void testExtractWordsFromWordAttachment() throws Exception {
		File inputFile = FileUtils.toFile(AttachmentExtractorTest.class
				.getResource("/testExtractWordsFromWordAttachment.eml"));
		File assertFile = FileUtils.toFile(AttachmentExtractorTest.class
				.getResource("/testExtractWordsFromWordAttachment.txt"));

		InputStream is = new FileInputStream(inputFile);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.loadMimeMessage(mimeMessage);

		IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
				mailMessage);

		String parsedContent = indexedMailMessage.getAttachments()[0].getBody()
				.replaceAll("[ \\t\\n\\r]+", " ");

		String assertContent = FileUtils.readFileToString(assertFile)
				.replaceAll("[ \\t\\n\\r]+", " ");

		Assert.assertEquals(assertContent, parsedContent);
	}

	@Test
	public void testExtractWordsFromPDFAttachment() throws Exception {
		File inputFile = FileUtils.toFile(AttachmentExtractorTest.class
				.getResource("/testExtractWordsFromPDFAttachment.eml"));
		File assertFile1 = FileUtils.toFile(AttachmentExtractorTest.class
				.getResource("/testExtractWordsFromPDFAttachment.txt"));

		InputStream is = new FileInputStream(inputFile);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.loadMimeMessage(mimeMessage);

		IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
				mailMessage);

		String parsedContent1 = indexedMailMessage.getAttachments()[0]
				.getBody().replaceAll("[ \\t\\n\\r]+", " ");

		String assertContent1 = FileUtils.readFileToString(assertFile1)
				.replaceAll("[ \\t\\n\\r]+", " ");

		Assert.assertEquals(assertContent1, parsedContent1);

	}

	@Test
	public void testExtractWordsFromPSTAttachment() throws Exception {
		File assertFile1 = FileUtils.toFile(AttachmentExtractorTest.class
				.getResource("/testExtractWordsFromPSTAttachment.txt"));

		MailMessage pstMessage = testModel.generatePSTMessage();

		IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
				pstMessage);

		String parsedContent1 = indexedMailMessage.getAttachments()[0]
				.getBody().replaceAll("[ \\t\\n\\r]+", " ");

		String assertContent1 = FileUtils.readFileToString(assertFile1)
				.replaceAll("[ \\t\\n\\r]+", " ");

		Assert.assertEquals(assertContent1, parsedContent1);

	}

	@Test
	public void testJSON() throws Exception {
		File inputFile = FileUtils.toFile(AttachmentExtractorTest.class
				.getResource("/testJSON.eml"));
		File assertFile = FileUtils.toFile(AttachmentExtractorTest.class
				.getResource("/testJSON.txt"));

		InputStream is = new FileInputStream(inputFile);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.loadMimeMessage(mimeMessage);

		IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
				mailMessage);
		String json = IndexedMailMessage.toJSON(indexedMailMessage).replaceAll(
				"[ \\\\t\\\\n\\\\r]+", " ");

		System.out.println(json);

		String assertContent = FileUtils.readFileToString(assertFile)
				.replaceAll("[ \\\\t\\\\n\\\\r]+", " ");
		System.out.println(assertContent);
		Assert.assertEquals(assertContent, json);
	}

	@Test
	public void testInvalidAttachment() throws Exception {
		File inputFile = FileUtils.toFile(AttachmentExtractorTest.class
				.getResource("/testInvalidAttachment.eml"));

		InputStream is = new FileInputStream(inputFile);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.loadMimeMessage(mimeMessage);

		IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
				mailMessage);

		String parsedContent = indexedMailMessage.getAttachments()[0].getBody();

		Assert.assertEquals(StringUtils.EMPTY, parsedContent);

	}

}
