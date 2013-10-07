package com.reqo.ironhold.reader.bloomberg.model;

import com.reqo.ironhold.reader.bloomberg.model.bloomberg.converters.ConversationConverter;
import com.reqo.ironhold.reader.bloomberg.model.ib.*;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * User: ilya
 * Date: 9/25/13
 * Time: 9:43 AM
 */
public class ConversationTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testProcessXMLFile() throws Exception {
        File ibFile = FileUtils.toFile(ConversationTest.class
                .getResource("/a30066168.ib.130604.xml"));

        File attFile = FileUtils.toFile(MessagesTest.class
                .getResource("/a30066168.att.130604.tar.gz"));

        JAXBContext jaxbContext = JAXBContext.newInstance(FileDump.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();


        FileDump conversations = (FileDump) jaxbUnmarshaller.unmarshal(ibFile);
        ConversationConverter cc = new ConversationConverter();

        for (Conversation conversation : conversations.getConversation()) {
            MimeMailMessage mimeMessage = cc.convert(conversation, null, attFile.getAbsolutePath());

            String emlFilePath = temporaryFolder.getRoot() + File.separator + mimeMessage.getMessageId() + ".eml";
            File emlFile = new File(emlFilePath);
            FileUtils.writeStringToFile(emlFile, mimeMessage.getRawContents());

            Assert.assertTrue(emlFile.exists());
            Assert.assertTrue(emlFile.length() > 0);

        }

        Assert.assertTrue(temporaryFolder.getRoot().exists());
    }
}
