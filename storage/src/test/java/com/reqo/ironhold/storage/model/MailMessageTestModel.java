package com.reqo.ironhold.storage.model;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;

import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.reqo.ironhold.storage.IStorageService;

public class MailMessageTestModel extends CommonTestModel {

	private static final String PST_TEST_FILE = "/data.pst";
	private static List<MailMessage> mailMessages = new ArrayList<MailMessage>();
	private static List<PSTMessage> pstMessages = new ArrayList<PSTMessage>();
	private static final int MAX_MESSAGES_TO_LOAD = 10;
	static {
		try {

			File file = FileUtils.toFile(MailMessageTestModel.class
					.getResource(PST_TEST_FILE));
			System.out.println("Loading messages from " + file);
			PSTFile pstFile;
			pstFile = new PSTFile(file);

			loadAllMessages("", pstFile.getRootFolder());

			for (PSTMessage pstMessage : pstMessages) {
				if ()
				mailMessages.add(new MailMessage(pstMessage,
						new PSTMessageSource(file.toString(), file.length(),
								new Date(file.lastModified()), new Date(),
								InetAddress.getLocalHost().getHostName())));
			}

			System.out.println("Loaded " + mailMessages.size() + " files");
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

	private static void loadAllMessages(String folderPath, PSTFolder folder) throws PSTException,
			IOException {
		if (pstMessages.size() == MAX_MESSAGES_TO_LOAD) {
			return;
		}
		
		System.out.println(folderPath);
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
				pstMessages.add(message);
				if (pstMessages.size() == MAX_MESSAGES_TO_LOAD) {
					return;
				}

			}
		}
	}

	public static MailMessage verifyStorage(IStorageService storageService,
			MailMessage inputMessage) throws Exception {

		MailMessage storedMessage = storageService.getMailMessage(
				inputMessage.getMessageId(), true);

		Assert.assertEquals(MailMessage.toJSON(inputMessage),
				MailMessage.toJSON(storedMessage));
		Assert.assertNotNull(storedMessage.getStoredDate());

		return storedMessage;
	}

	public static PSTMessageSource generatePSTMessageSource() {
		return new PSTMessageSource(df.getRandomWord() + ".pst",
				df.getNumberBetween(1, 10000), df.getBirthDate(),
				df.getBirthDate(), df.getRandomWord());
	}

}
