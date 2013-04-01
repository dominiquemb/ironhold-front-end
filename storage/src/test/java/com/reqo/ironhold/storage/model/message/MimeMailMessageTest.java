package com.reqo.ironhold.storage.model.message;

import com.pff.PSTException;
import com.pff.PSTMessage;
import com.reqo.ironhold.storage.model.PSTMessageTestModel;
import com.reqo.ironhold.storage.utils.ChecksumUtils;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.mail.EmailException;
import org.elasticsearch.common.Base64;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;

/**
 * User: ilya
 * Date: 3/14/13
 * Time: 8:12 PM
 */
public class MimeMailMessageTest {
    private PSTMessageTestModel testModel;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        testModel = new PSTMessageTestModel("/data.pst");
    }

    @Test
    public void testGetMimeMessage() throws Exception {
        PSTMessageTestModel testModel = new PSTMessageTestModel("/ilya.pst", 500);

        for (PSTMessage pstMessage : testModel.generateOriginalPSTMessages()) {
            System.out.println("Loading \"" + pstMessage.getSubject() + "\" " + pstMessage.getInternetMessageId());
            MimeMailMessage mimeMailMessage = MimeMailMessage.getMimeMailMessage(pstMessage);


            for (Attachment attachment : mimeMailMessage.getAttachments()) {
                System.out.println("Normalized dir name: " + FilenameUtils.normalize(mimeMailMessage.getMessageId()));
                File actualFile = new File(tempFolder.getRoot().getAbsolutePath() + File.separator + FilenameUtils.normalize(mimeMailMessage.getMessageId()) + File.separator + attachment.getFileName());
                FileUtils.writeByteArrayToFile(actualFile, Base64.decode(attachment.getBody()));

                File expectedFile = FileUtils.toFile(MimeMailMessageTest.class
                        .getResource("/attachments/" + FilenameUtils.normalize(mimeMailMessage.getMessageId()) + "/" + attachment.getFileName()));

                System.out.println("Comparing " + actualFile.length() + " v " + expectedFile.length() + " byte file " + actualFile.getAbsolutePath() + " against " + expectedFile.getAbsolutePath());


                String actualChecksum = ChecksumUtils.getMD5Checksum(actualFile);


                String expectedChecksum = ChecksumUtils.getMD5Checksum(expectedFile);

                Assert.assertEquals(expectedChecksum, actualChecksum);
            }
            //System.out.println(mimeMailMessage.getRawContents());


        }
    }


    @Test
    public void testAttachmentsAbsence() throws IOException, PSTException, MessagingException, EmailException {
        testModel = new PSTMessageTestModel("/data.pst");

        MimeMailMessage pstMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        Assert.assertTrue(pstMessage.getAttachments().length == 0);


    }

    @Test
    public void testAttachmentsPresence() throws IOException, PSTException, MessagingException, EmailException {
        testModel = new PSTMessageTestModel("/attachments.pst");

        MimeMailMessage pstMessage = MimeMailMessage.getMimeMailMessage(testModel.generateOriginalPSTMessage());

        Assert.assertTrue(pstMessage.getAttachments().length > 0);

        String serializedMessage = pstMessage.getRawContents();

        Assert.assertFalse(serializedMessage.contains("\"attachments\":"));

    }


    @Test
    public void testAttachmentsSerialization() throws IOException, PSTException, MessagingException, EmailException {
        testModel = new PSTMessageTestModel("/attachments.pst");

        PSTMessage originalPSTMessage = testModel.generateOriginalPSTMessage();
        MimeMailMessage pstMessage = MimeMailMessage.getMimeMailMessage(originalPSTMessage);

        Assert.assertTrue(pstMessage.getAttachments().length > 0);

        Attachment[] attachments = pstMessage.getAttachments();

        Assert.assertTrue(attachments.length > 0);

        Assert.assertEquals(originalPSTMessage.getNumberOfAttachments(), pstMessage.getAttachments().length);

        for (int i = 0; i < pstMessage.getAttachments().length; i++) {
            Assert.assertEquals(originalPSTMessage.getAttachment(i).getLongFilename(), pstMessage.getAttachments()[i].getFileName());
        }
    }

}
