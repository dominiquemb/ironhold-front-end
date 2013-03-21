package com.reqo.ironhold.search.es;

import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedMailMessage;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class AttachmentExtractorTest {
    private PSTMessageTestModel testModel;

    @Before
    public void setUp() throws Exception {
        testModel = new PSTMessageTestModel("/attachments.pst");
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

        MimeMailMessage pstMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

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
        String json = IndexedMailMessage.toJSON(indexedMailMessage).replace("\\n","").replace(" ","");

        String assertContent = FileUtils.readFileToString(assertFile)
                .replace("\\n","").replace(" ","");
        Assert.assertEquals(StringUtils.deleteWhitespace(assertContent), StringUtils.deleteWhitespace(json));
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
