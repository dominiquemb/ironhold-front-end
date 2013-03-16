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
import java.util.*;

public class MailMessageTestModel extends CommonTestModel {
	private static final int MAX_MESSAGES_TO_LOAD = 5;
    private final int maxMessagesToLoad;
	private List<MailMessage> mailMessages = new ArrayList<MailMessage>();
	private List<PSTMessage> pstMessages = new ArrayList<PSTMessage>();

    public MailMessageTestModel(String pstFilePath, int maxMessagesToLoad)  throws IOException, PSTException {
        this.maxMessagesToLoad = maxMessagesToLoad;
        File file = FileUtils.toFile(MailMessageTestModel.class
                .getResource(pstFilePath));
        System.out.println("Loading messages from " + file);
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

        System.out.println("Loaded " + mailMessages.size() + " messages");
    }
	public MailMessageTestModel(String pstFilePath) throws IOException, PSTException {
        this(pstFilePath, MAX_MESSAGES_TO_LOAD);
	}

    public PSTMessage generateOriginalPSTMessage() {
        return pstMessages.get(0);
    }

	public MailMessage generatePSTMessage() {

		return mailMessages.get(0);
	}

	public List<MailMessage> generatePSTMessages() {

		return mailMessages;
	}

	private void loadAllMessages(String folderPath, PSTFolder folder)
			throws PSTException, IOException {
		if (pstMessages.size() == maxMessagesToLoad) {
			return;
		}

		if (folder.hasSubfolders()) {
			Vector<PSTFolder> childFolders = folder.getSubFolders();
			for (PSTFolder childFolder : childFolders) {
				loadAllMessages(
						folderPath + "/" + childFolder.getDisplayName(),
						childFolder);
				if (pstMessages.size() == maxMessagesToLoad) {
					return;
				}
			}
		}

		if (folder.getContentCount() > 0) {
			PSTMessage message = (PSTMessage) folder.getNextChild();
			while (message != null) {
				System.out.println(message.getInternetMessageId() + " : "
						+ message.getNumberOfAttachments());

				pstMessages.add(message);
				if (pstMessages.size() == maxMessagesToLoad) {
					return;
				}
				message = (PSTMessage) folder.getNextChild();
			}
		}
	}

	public MailMessage verifyStorage(IStorageService storageService,
			MailMessage inputMessage) throws Exception {

		MailMessage storedMessage = storageService.getMailMessage(
				inputMessage.getMessageId(), true);
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

		return storedMessage;
	}

    public List<PSTMessage> generateOriginalPSTMessages() {
        return pstMessages;
    }
}
