package com.reqo.ironhold.search.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.junit.Ignore;
import org.junit.Test;

import com.reqo.ironhold.search.model.IndexedMailMessage;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MimeMailMessage;

public class AttachmentExtractorTest {

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

		MailMessage pstMessage = MailMessageTestModel.generatePSTMessage();

		IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
				pstMessage);

		String parsedContent1 = indexedMailMessage.getAttachments()[0]
				.getBody().replaceAll("[ \\t\\n\\r]+", " ");

		String assertContent1 = FileUtils.readFileToString(assertFile1)
				.replaceAll("[ \\t\\n\\r]+", " ");

		Assert.assertEquals(assertContent1, parsedContent1);

	}

	@Test
	@Ignore
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
		String json = IndexedMailMessage.toJSON(indexedMailMessage).replaceAll("[ \\t\\n\\r]+", " ");

		System.out.println(json);

		String assertContent = FileUtils.readFileToString(assertFile)
				.replaceAll("[ \\t\\n\\r]+", " ");

		Assert.assertEquals(assertContent, json);
	}

}
