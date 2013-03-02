package com.reqo.ironhold.exporter.model;

import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.model.IMAPMessageSource;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.PSTMessageSource;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

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
				System.out.println(message.getInternetMessageId() + " : "
						+ message.getNumberOfAttachments());

				pstMessages.add(message);
				if (pstMessages.size() == MAX_MESSAGES_TO_LOAD) {
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
}
