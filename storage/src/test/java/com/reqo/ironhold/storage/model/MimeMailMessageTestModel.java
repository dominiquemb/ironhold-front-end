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

public class MimeMailMessageTestModel extends CommonTestModel {

	
	public static MimeMailMessage verifyStorage(IStorageService storageService,
			MimeMailMessage inputMessage) throws Exception {

		MimeMailMessage storedMessage = storageService.getMimeMailMessage(
				inputMessage.getMessageId());
		Assert.assertEquals(MimeMailMessage.serialize(inputMessage),
				MimeMailMessage.serialize(storedMessage));
		Assert.assertEquals(inputMessage.getAttachments().length,
				storedMessage.getAttachments().length);
		Assert.assertEquals(MailMessage.serializeAttachments(inputMessage
				.getAttachments()), MailMessage
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
		Assert.assertEquals(MailMessage.serializeAttachments(expected
				.getAttachments()), MailMessage
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