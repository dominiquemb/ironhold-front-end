package com.reqo.ironhold.search.es;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.message.source.PSTMessageSource;
import junit.framework.Assert;

import java.io.IOException;

public class MimeMailMessageTestModel extends CommonTestModel {

	
	public static MimeMailMessage verifyStorage(IStorageService storageService,
			MimeMailMessage inputMessage) throws Exception {

		MimeMailMessage storedMessage = storageService.getMimeMailMessage(
				inputMessage.getMessageId());
		Assert.assertEquals(MimeMailMessage.serialize(inputMessage),
				MimeMailMessage.serialize(storedMessage));
		Assert.assertEquals(inputMessage.getAttachments().length,
				storedMessage.getAttachments().length);
		Assert.assertEquals(MimeMailMessage.serializeAttachments(inputMessage
				.getAttachments()), MimeMailMessage
				.serializeAttachments(storedMessage.getAttachments()));
		Assert.assertNotNull(storedMessage.getStoredDate());

		for (int i = 0; i < inputMessage.getSources().length; i++) {
			if (inputMessage.getSources()[i] instanceof PSTMessageSource) {
				Assert.assertTrue(PSTMessageSource.sameAs((PSTMessageSource)inputMessage.getSources()[i], (PSTMessageSource)storedMessage.getSources()[i]));
			} else if  (inputMessage.getSources()[i] instanceof IMAPMessageSource) {
				Assert.assertTrue(IMAPMessageSource.sameAs((IMAPMessageSource)inputMessage.getSources()[i], (IMAPMessageSource)storedMessage.getSources()[i]));
			}
		}

		return storedMessage;
	}
	
	public static void verifyMimeMailMessage(MimeMailMessage expected, MimeMailMessage actual) throws IOException {
		Assert.assertEquals(MimeMailMessage.serialize(expected),
				MimeMailMessage.serialize(actual));
		Assert.assertEquals(expected.getAttachments().length,
				actual.getAttachments().length);
		Assert.assertEquals(MimeMailMessage.serializeAttachments(expected
				.getAttachments()), MimeMailMessage
				.serializeAttachments(actual.getAttachments()));
		
		for (int i = 0; i < expected.getSources().length; i++) {
			if (expected.getSources()[i] instanceof PSTMessageSource) {
				Assert.assertTrue(PSTMessageSource.sameAs((PSTMessageSource)expected.getSources()[i], (PSTMessageSource)actual.getSources()[i]));
			} else if  (expected.getSources()[i] instanceof IMAPMessageSource) {
				Assert.assertTrue(IMAPMessageSource.sameAs((IMAPMessageSource)expected.getSources()[i], (IMAPMessageSource)actual.getSources()[i]));
			}
		}


	}

}