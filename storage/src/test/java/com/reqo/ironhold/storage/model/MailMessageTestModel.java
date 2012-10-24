package com.reqo.ironhold.storage.model;

import java.util.UUID;

import junit.framework.Assert;

import com.reqo.ironhold.storage.IStorageService;

public class MailMessageTestModel extends CommonTestModel {

	public static MailMessage generate() {
		MailMessage testMessage = new MailMessage();
		testMessage.setAttachments(AttachmentTestModel.generateAttachments());

		testMessage.setBcc(RecipientTestModel.generateRecipients());
		testMessage.setCc(RecipientTestModel.generateRecipients());
		testMessage.setTo(RecipientTestModel.generateRecipients());
		testMessage.setBody(generateText());
		testMessage.setFrom(df.getName());
		testMessage.setMessageId(UUID.randomUUID().toString());
		testMessage.setRecievedDate(df.getDateBetween(getMinDate(),
				getMaxDate()));
		testMessage.setSources(generateNames());
		testMessage.setSubject(generateText());

		return testMessage;
	}

	public static MailMessage verifyStorage(IStorageService storageService, MailMessage inputMessage)
			throws Exception {

		MailMessage storedMessage = storageService.getMailMessage(
				inputMessage.getMessageId(), true);

		Assert.assertEquals(MailMessage.toJSON(inputMessage),
				MailMessage.toJSON(storedMessage));
		Assert.assertEquals(inputMessage, storedMessage);
		Assert.assertNotNull(storedMessage.getStoredDate());

		return storedMessage;
	}

}
