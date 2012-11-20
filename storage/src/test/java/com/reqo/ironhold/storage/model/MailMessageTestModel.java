package com.reqo.ironhold.storage.model;

import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.reqo.ironhold.storage.IStorageService;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class MailMessageTestModel extends CommonTestModel {

    private static final String PST_TEST_FILE = "/data.pst";
    private static List<MailMessage> mailMessages = new ArrayList<MailMessage>();
    private static List<PSTMessage> pstMessages = new ArrayList<PSTMessage>();
    private static final int MAX_MESSAGES_TO_LOAD = 10;

    static {
        try {

            File file = FileUtils.toFile(MailMessageTestModel.class.getResource(PST_TEST_FILE));
            System.out.println("Loading messages from " + file);
            PSTFile pstFile;
            pstFile = new PSTFile(file);

            loadAllMessages("", pstFile.getRootFolder());

            Set<String> uniqueMessages = new HashSet<String>();
            for (PSTMessage pstMessage : pstMessages) {
                if (!uniqueMessages.contains(pstMessage.getInternetMessageId())) {
                    mailMessages.add(new MailMessage(pstMessage, new PSTMessageSource(file.toString(), "",
                            file.length(), new Date(file.lastModified()), new Date(),
                            InetAddress.getLocalHost().getHostName())));
                }
            }

            System.out.println("Loaded " + mailMessages.size() + " messages");
        } catch (PSTException | IOException e) {
            e.printStackTrace();
        }
    }

    public static MailMessage generatePSTMessage() {

        return mailMessages.get(0);
    }

    public static List<MailMessage> generatePSTMessages() {

        return mailMessages;
    }

    private static void loadAllMessages(String folderPath, PSTFolder folder) throws PSTException, IOException {
        if (pstMessages.size() == MAX_MESSAGES_TO_LOAD) {
            return;
        }

        if (folder.hasSubfolders()) {
            Vector<PSTFolder> childFolders = folder.getSubFolders();
            for (PSTFolder childFolder : childFolders) {
                loadAllMessages(folderPath + "/" + childFolder.getDisplayName(), childFolder);
                if (pstMessages.size() == MAX_MESSAGES_TO_LOAD) {
                    return;
                }
            }
        }

        if (folder.getContentCount() > 0) {
            PSTMessage message = (PSTMessage) folder.getNextChild();
            while (message != null) {
                System.out.println(message.getInternetMessageId() + " : " + message.getNumberOfAttachments());

                pstMessages.add(message);
                if (pstMessages.size() == MAX_MESSAGES_TO_LOAD) {
                    return;
                }
                message = (PSTMessage) folder.getNextChild();
            }
        }
    }

    public static MailMessage verifyStorage(IStorageService storageService, MailMessage inputMessage) throws Exception {

        MailMessage storedMessage = storageService.getMailMessage(inputMessage.getMessageId(), true);
        Assert.assertEquals(MailMessage.serializeMailMessage(inputMessage), MailMessage.serializeMailMessage
                (storedMessage));
        Assert.assertNotNull(storedMessage.getStoredDate());

        return storedMessage;
    }

    public static PSTMessageSource generatePSTMessageSource() {
        return new PSTMessageSource(df.getRandomWord() + ".pst", df.getRandomWord(), df.getNumberBetween(1, 10000), df.getBirthDate(), df.getBirthDate(), df.getRandomWord());
    }

}
