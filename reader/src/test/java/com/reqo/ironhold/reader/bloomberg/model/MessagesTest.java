package com.reqo.ironhold.reader.bloomberg.model;

import com.reqo.ironhold.reader.bloomberg.BloombergReader;
import com.reqo.ironhold.reader.bloomberg.model.bloomberg.converters.MessageConverter;
import com.reqo.ironhold.reader.bloomberg.model.dscl.DisclaimerType;
import com.reqo.ironhold.reader.bloomberg.model.dscl.FileDumpType;
import com.reqo.ironhold.reader.bloomberg.model.msg.FileDump;
import com.reqo.ironhold.reader.bloomberg.model.msg.Message;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;


/**
 * User: ilya
 * Date: 6/6/13
 * Time: 8:38 AM
 */
public class MessagesTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testProcessXMLFile() throws Exception {
        File msgFile = FileUtils.toFile(MessagesTest.class
                .getResource("/a30066168.msg.130604.xml"));

        File dsclFile = FileUtils.toFile(MessagesTest.class
                .getResource("/a30066168.dscl.130604.xml"));

        File attFile = FileUtils.toFile(MessagesTest.class
                .getResource("/a30066168.att.130604.tar.gz"));

        JAXBContext msgJaxbContext = JAXBContext.newInstance(FileDump.class);
        JAXBContext dsclJaxbContext = JAXBContext.newInstance(FileDumpType.class);

        Unmarshaller msgJaxbUnmarshaller = msgJaxbContext.createUnmarshaller();
        Unmarshaller dsclJaxbUnmarshaller = dsclJaxbContext.createUnmarshaller();


        FileDumpType disclaimers = (FileDumpType) dsclJaxbUnmarshaller.unmarshal(dsclFile);
        FileDump messages = (FileDump) msgJaxbUnmarshaller.unmarshal(msgFile);

        for (Message message : messages.getMessage()) {
            MessageConverter mc = new MessageConverter();

            MimeMailMessage mimeMessage = mc.convert(message, BloombergReader.getDisclaimer(disclaimers, message), attFile.getAbsolutePath());

            String emlFilePath = temporaryFolder.getRoot() + File.separator + mimeMessage.getMessageId() + ".eml";
            File emlFile = new File(emlFilePath);
            FileUtils.writeStringToFile(emlFile, mimeMessage.getRawContents());

           // System.out.println(mimeMessage.getRawContents());
            Assert.assertTrue(emlFile.exists());
            Assert.assertTrue(emlFile.length() > 0);

        }

        Assert.assertTrue(temporaryFolder.getRoot().exists());
    }

    @Test
    public void testProcessingDisclaimers() throws Exception {
        File dsclFile = FileUtils.toFile(MessagesTest.class
                .getResource("/a30066168.dscl.130604.xml"));

        JAXBContext dsclJaxbContext = JAXBContext.newInstance(FileDumpType.class);

        Unmarshaller dsclJaxbUnmarshaller = dsclJaxbContext.createUnmarshaller();


        FileDumpType disclaimers = (FileDumpType) dsclJaxbUnmarshaller.unmarshal(dsclFile);

        for (DisclaimerType disclaimerType : disclaimers.getDisclaimer()) {
            Assert.assertNotNull(disclaimerType.getDisclaimerReference());
            Assert.assertNotNull(disclaimerType.getDisclaimerText());
        }

        Assert.assertEquals(19, disclaimers.getDisclaimer().size());
    }


}
