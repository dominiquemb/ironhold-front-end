package com.reqo.ironhold.testcommon;

import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.reqo.ironhold.model.message.eml.IMAPMessageSource;
import com.reqo.ironhold.model.message.pst.MailMessage;
import com.reqo.ironhold.model.message.pst.PSTMessageSource;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MailMessageTestModel extends CommonTestModel {
    private static final int MAX_MESSAGES_TO_LOAD = 5;

    private List<MailMessage> mailMessages = new ArrayList<MailMessage>();
    private List<PSTMessage> pstMessages = new ArrayList<PSTMessage>();

    public MailMessageTestModel(String pstFilePath) throws JsonParseException, JsonMappingException, JsonGenerationException, IOException, PSTException {
        File file = FileUtils.toFile(MailMessageTestModel.class
                .getResource(pstFilePath));
        PSTFile pstFile;
        pstFile = new PSTFile(file);

        loadAllMessages("", pstFile.getRootFolder());

        Set<String> uniqueMessages = new HashSet<String>();
        for (PSTMessage pstMessage : pstMessages) {
            if (!uniqueMessages.contains(pstMessage.getInternetMessageId())) {
                mailMessages.add(new MailMessage(pstMessage,
                        new PSTMessageSource(file.toString(), "", file
                                .length(), new Date(file.lastModified()))));
            }
        }
    }

    public MailMessage generatePSTMessage() {

        return mailMessages.get(0);
    }

    public List<MailMessage> generatePSTMessages() {

        return mailMessages;
    }

    private void loadAllMessages(String folderPath, PSTFolder folder)
            throws PSTException, IOException {
        if (pstMessages.size() == MAX_MESSAGES_TO_LOAD) {
            return;
        }

        if (folder.hasSubfolders()) {
            Vector<PSTFolder> childFolders = folder.getSubFolders();
            for (PSTFolder childFolder : childFolders) {
                loadAllMessages(
                        folderPath + "/" + childFolder.getDisplayName(),
                        childFolder);
                if (pstMessages.size() == MAX_MESSAGES_TO_LOAD) {
                    return;
                }
            }
        }

        if (folder.getContentCount() > 0) {
            PSTMessage message = (PSTMessage) folder.getNextChild();
            while (message != null) {
                pstMessages.add(message);
                if (pstMessages.size() == MAX_MESSAGES_TO_LOAD) {
                    return;
                }
                message = (PSTMessage) folder.getNextChild();
            }
        }
    }

    public MailMessage verifyStorage(MailMessage storedMessage,
                                     MailMessage inputMessage) throws Exception {

        Assert.assertEquals(MailMessage.serializeMailMessage(inputMessage),
                MailMessage.serializeMailMessage(storedMessage));
        Assert.assertEquals(inputMessage.getAttachments().length,
                storedMessage.getAttachments().length);
        Assert.assertEquals(MailMessage.serializeAttachments(inputMessage
                .getAttachments()), MailMessage
                .serializeAttachments(storedMessage.getAttachments()));
        Assert.assertNotNull(storedMessage.getStoredDate());

        for (int i = 0; i < inputMessage.getSources().length; i++) {
            if (inputMessage.getSources()[i] instanceof PSTMessageSource) {
                Assert.assertTrue(PSTMessageSource.sameAs(
                        (PSTMessageSource) inputMessage.getSources()[i],
                        (PSTMessageSource) storedMessage.getSources()[i]));
            } else if (inputMessage.getSources()[i] instanceof IMAPMessageSource) {
                Assert.assertTrue(IMAPMessageSource.sameAs(
                        (IMAPMessageSource) inputMessage.getSources()[i],
                        (IMAPMessageSource) storedMessage.getSources()[i]));
            }
        }

        for (int i = 0; i < inputMessage.getAttachments().length; i++) {
            Assert.assertEquals(inputMessage.getAttachments()[i].getBody(), storedMessage.getAttachments()[i].getBody());
            Assert.assertEquals(inputMessage.getAttachments()[i].getFileExt(), storedMessage.getAttachments()[i].getFileExt());
            Assert.assertEquals(inputMessage.getAttachments()[i].getFileName(), storedMessage.getAttachments()[i].getFileName());
            Assert.assertEquals(inputMessage.getAttachments()[i].getSize(), storedMessage.getAttachments()[i].getSize());

            Assert.assertNotNull(inputMessage.getAttachments()[i].getFileExt());
        }
        return storedMessage;
    }
}
