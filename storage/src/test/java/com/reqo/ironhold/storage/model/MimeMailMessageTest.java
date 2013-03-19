package com.reqo.ironhold.storage.model;

import com.pff.PSTMessage;
import com.reqo.ironhold.storage.utils.ChecksumUtils;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.common.Base64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * User: ilya
 * Date: 3/14/13
 * Time: 8:12 PM
 */
public class MimeMailMessageTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();


    @Test
    public void testGetMimeMessage1() throws Exception {
        PSTMessageTestModel testModel = new PSTMessageTestModel("/ilya.pst", 500);

        for (PSTMessage pstMessage : testModel.generateOriginalPSTMessages()) {
            System.out.println("Loading \"" + pstMessage.getSubject() + "\" " + pstMessage.getInternetMessageId());
            MimeMailMessage mimeMailMessage = MimeMailMessage.getMimeMailMessage(pstMessage);


            for (Attachment attachment : mimeMailMessage.getAttachments()) {
                System.out.println("Checking attachment in " + mimeMailMessage.getExportFileName(null) + "/" + attachment.getFileName());

                File actualFile = new File(tempFolder.getRoot().getAbsolutePath() + File.separator + mimeMailMessage.getExportFileName(null) + File.separator + attachment.getFileName());
                FileUtils.writeByteArrayToFile(actualFile, Base64.decode(attachment.getBody()));

                File expectedFile = FileUtils.toFile(MimeMailMessageTest.class
                        .getResource("/attachments/" + mimeMailMessage.getExportFileName(null) + "/" + attachment.getFileName()));

                System.out.println("Comparing " + actualFile.length() + " v " + expectedFile.length() + " byte file " + actualFile.getAbsolutePath() + " against " + expectedFile.getAbsolutePath());



                String actualChecksum = ChecksumUtils.getMD5Checksum(actualFile);


                String expectedChecksum = ChecksumUtils.getMD5Checksum(expectedFile);

                Assert.assertEquals(expectedChecksum, actualChecksum);
            }
            //System.out.println(mimeMailMessage.getRawContents());


        }
    }

}
