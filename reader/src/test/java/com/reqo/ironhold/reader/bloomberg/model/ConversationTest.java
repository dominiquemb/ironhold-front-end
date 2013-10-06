package com.reqo.ironhold.reader.bloomberg.model;

import com.reqo.ironhold.reader.bloomberg.model.ib.*;
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

        JAXBContext jaxbContext = JAXBContext.newInstance(FileDump.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();


        FileDump conversations = (FileDump) jaxbUnmarshaller.unmarshal(ibFile);

        for (Conversation conversation : conversations.getConversation()) {
            System.out.println(conversation.getStartTime().getContent().get(0) + " -> " + conversation.getEndTime().getContent().get(0));
            for (Object o : conversation.getAttachmentOrInviteOrParticipantEntered()) {
                if (o instanceof Attachment) {
                    Attachment attachment = (Attachment) o;
                    System.out.println(attachment.getDateTime().getContent().get(0) + " Attachment " + attachment.getConversationID().getContent().get(0) + ": " + attachment.getUser().getFirstName().getContent().get(0) + " " + attachment.getUser().getLastName().getContent().get(0) + " > " + attachment.getFileName().getContent().get(0));
                } else if (o instanceof Invite) {
                    Invite invite = (Invite) o;
                    System.out.println(invite.getDateTime().getContent().get(0) + " Invite " + invite.getConversationID().getContent().get(0));
                } else if (o instanceof ParticipantEntered) {
                    ParticipantEntered participantEntered = (ParticipantEntered) o;
                    System.out.println(participantEntered.getDateTime().getContent().get(0) + " ParticipantEntered " + participantEntered.getConversationID().getContent().get(0) + ": " + participantEntered.getUser().getFirstName().getContent().get(0) + " " + participantEntered.getUser().getLastName().getContent().get(0));
                } else if (o instanceof ParticipantLeft) {
                    ParticipantLeft participantLeft = (ParticipantLeft) o;
                    System.out.println(participantLeft.getDateTime().getContent().get(0) + " ParticipantLeft " + participantLeft.getConversationID().getContent().get(0) + ": " + participantLeft.getUser().getFirstName().getContent().get(0) + " " + participantLeft.getUser().getLastName().getContent().get(0));
                } else if (o instanceof Message) {
                    Message message = (Message) o;
                    System.out.println(message.getDateTime().getContent().get(0) + " Message " + message.getConversationID().getContent().get(0) + ": " + message.getUser().getFirstName().getContent().get(0) + " " + message.getUser().getLastName().getContent().get(0) + " > " + StringUtils.join(message.getContent().getContent(), "\n"));
                } else if (o instanceof History) {
                    History history = (History) o;
                    System.out.println(history.getDateTime().getContent().get(0) + " History " + history.getConversationID().getContent().get(0) + ": " + history.getUser().getFirstName().getContent().get(0) + " " + history.getUser().getLastName().getContent().get(0) + " > ");
                } else if (o instanceof SystemMessage) {
                    SystemMessage systemMessage = (SystemMessage) o;
                    System.out.println(systemMessage.getDateTime().getContent().get(0) + " SystemMessage " + systemMessage.getConversationID().getContent().get(0) + " > " + StringUtils.join(systemMessage.getContent().getContent(), "\n"));
                }

            }
            /*MessageConverter mc = new MessageConverter();

            MimeMailMessage mimeMessage = mc.convert(message, getDisclaimer(disclaimers, message), attFile);

            String emlFilePath = temporaryFolder.getRoot() + File.separator + mimeMessage.getMessageId() + ".eml";
            File emlFile = new File(emlFilePath);
            FileUtils.writeStringToFile(emlFile, mimeMessage.getRawContents());

            Assert.assertTrue(emlFile.exists());
            Assert.assertTrue(emlFile.length() > 0);   */

        }

        Assert.assertTrue(temporaryFolder.getRoot().exists());
    }
}
