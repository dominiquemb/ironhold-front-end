package com.reqo.ironhold.storage.model.message.search;

import com.reqo.ironhold.storage.model.PSTMessageTestModel;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedMailMessage;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;

public class IndexedMailMessageTest {
    @Rule
    public TemporaryFolder parentFolder = new TemporaryFolder();

    private PSTMessageTestModel testModel;
    private static final String TEST_CLIENT = "test";
    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    private SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMdd");

    @Before
    public void setUp() throws Exception {
        testModel = new PSTMessageTestModel("/attachments.pst");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testIndexedMailMessageConstructor() throws Exception {
        MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                inputMessage, true);

        Assert.assertEquals(inputMessage
                .getMessageId(), indexedMailMessage.getMessageId());

        Assert.assertEquals(StringUtils.deleteWhitespace(inputMessage.getBody()),
                StringUtils.deleteWhitespace(indexedMailMessage.getBody()));
        Assert.assertEquals(inputMessage.getSubject(),
                indexedMailMessage.getSubject());
        Assert.assertEquals(inputMessage
                .getMessageDate(), indexedMailMessage
                .getMessageDate());
        Assert.assertEquals(inputMessage
                .getFrom().getAddress(), indexedMailMessage.getSender()
                .getAddress());
        Assert.assertEquals(inputMessage.getSize(),
                indexedMailMessage.getSize());
        Assert.assertEquals(yearFormat.format(inputMessage.getMessageDate()), indexedMailMessage.getYear());
        Assert.assertEquals(monthDayFormat.format(inputMessage.getMessageDate()), indexedMailMessage.getMonthDay());

    }


    @Test
    public void testExtractWordsFromWordAttachment() throws Exception {
        File inputFile = FileUtils.toFile(IndexedMailMessageTest.class
                .getResource("/testExtractWordsFromWordAttachment.eml"));
        File assertFile = FileUtils.toFile(IndexedMailMessageTest.class
                .getResource("/testExtractWordsFromWordAttachment.txt"));

        InputStream is = new FileInputStream(inputFile);
        MimeMessage mimeMessage = new MimeMessage(null, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                mailMessage, true);

        String parsedContent = indexedMailMessage.getAttachments()[0].getBody()
                .replaceAll("[ \\t\\n\\r]+", " ");

        String assertContent = FileUtils.readFileToString(assertFile)
                .replaceAll("[ \\t\\n\\r]+", " ");

        Assert.assertEquals(assertContent, parsedContent);
    }

    @Test
    public void testExtractWordsFromPDFAttachment() throws Exception {
        File inputFile = FileUtils.toFile(IndexedMailMessageTest.class
                .getResource("/testExtractWordsFromPDFAttachment.eml"));
        File assertFile1 = FileUtils.toFile(IndexedMailMessageTest.class
                .getResource("/testExtractWordsFromPDFAttachment.txt"));

        InputStream is = new FileInputStream(inputFile);
        MimeMessage mimeMessage = new MimeMessage(null, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                mailMessage, true);

        String parsedContent1 = indexedMailMessage.getAttachments()[0]
                .getBody().replaceAll("[ \\t\\n\\r]+", " ");

        String assertContent1 = FileUtils.readFileToString(assertFile1)
                .replaceAll("[ \\t\\n\\r]+", " ");

        Assert.assertEquals(assertContent1, parsedContent1);

    }

    @Test
    public void testExtractWordsFromPSTAttachment() throws Exception {
        File assertFile1 = FileUtils.toFile(IndexedMailMessageTest.class
                .getResource("/testExtractWordsFromPSTAttachment.txt"));

        MimeMailMessage pstMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                pstMessage, true);

        String parsedContent1 = indexedMailMessage.getAttachments()[0]
                .getBody().replaceAll("[ \\t\\n\\r]+", " ");

        String assertContent1 = FileUtils.readFileToString(assertFile1)
                .replaceAll("[ \\t\\n\\r]+", " ");

        Assert.assertEquals(assertContent1, parsedContent1);

    }

    @Test
    public void testJSON() throws Exception {
        File inputFile = FileUtils.toFile(IndexedMailMessageTest.class
                .getResource("/testJSON.eml"));
        File assertFile = FileUtils.toFile(IndexedMailMessageTest.class
                .getResource("/testJSON.txt"));

        InputStream is = new FileInputStream(inputFile);
        MimeMessage mimeMessage = new MimeMessage(null, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                mailMessage, true);
        String json = indexedMailMessage.serialize().replace("\\n", "").replace(" ", "");

        String assertContent = FileUtils.readFileToString(assertFile)
                .replace("\\n", "").replace(" ", "");
        Assert.assertEquals(StringUtils.deleteWhitespace(assertContent), StringUtils.deleteWhitespace(json));
    }

    @Test
    public void testInvalidAttachment() throws Exception {
        File inputFile = FileUtils.toFile(IndexedMailMessageTest.class
                .getResource("/testInvalidAttachment.eml"));

        InputStream is = new FileInputStream(inputFile);
        MimeMessage mimeMessage = new MimeMessage(null, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                mailMessage, true);

        String parsedContent = indexedMailMessage.getAttachments()[0].getBody();

        Assert.assertEquals(StringUtils.EMPTY, parsedContent);

    }

    @Test
    public void testProblematicAttachment() throws Exception {
        File inputFile = FileUtils.toFile(IndexedMailMessageTest.class
                .getResource("/testProblematicAttachment.eml"));

        InputStream is = new FileInputStream(inputFile);
        MimeMessage mimeMessage = new MimeMessage(null, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                mailMessage, true);


        Assert.assertEquals(1, indexedMailMessage.getAttachments().length);
        Assert.assertEquals("1 DNC.pdf", indexedMailMessage.getAttachments()[0].getFileName());

        String json = indexedMailMessage.serialize();

        Assert.assertTrue(json.contains("1 DNC.pdf"));
    }

    @Test
    public void testDontExtractText() throws Exception {
        File inputFile = FileUtils.toFile(IndexedMailMessageTest.class
                .getResource("/testExtractWordsFromPDFAttachment.eml"));
        File assertFile1 = FileUtils.toFile(IndexedMailMessageTest.class
                .getResource("/testExtractWordsFromPDFAttachment.txt"));

        InputStream is = new FileInputStream(inputFile);
        MimeMessage mimeMessage = new MimeMessage(null, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        IndexedMailMessage indexedMailMessage = new IndexedMailMessage(
                mailMessage, false);

        String parsedContent1 = indexedMailMessage.getAttachments()[0]
                .getBody().replaceAll("[ \\t\\n\\r]+", " ");

        String assertContent1 = FileUtils.readFileToString(assertFile1)
                .replaceAll("[ \\t\\n\\r]+", " ");

        Assert.assertFalse(assertContent1.equals(parsedContent1));

    }
}
