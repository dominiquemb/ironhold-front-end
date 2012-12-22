package com.reqo.ironhold.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.mail.Header;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.fluttercode.datafactory.impl.DataFactory;
import org.junit.Test;

import com.reqo.ironhold.storage.model.IMAPMessageSource;
import com.reqo.ironhold.storage.model.MailMessage;

public class EmlLoadTest {

	private static IMAPMessageSource source;
	private static DataFactory df = new DataFactory();
	static {
		source = new IMAPMessageSource();
		source.setHostname(df.getRandomWord());
		source.setImapPort(df.getNumberBetween(1, 10000));
		source.setImapSource(df.getRandomWord());
		source.setLoadTimestamp(df.getBirthDate());
		source.setProtocol(df.getRandomWord());
		source.setUsername(df.getName());

	}

	@Test
	public void testMimeMessage() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testmessage.eml"));
		InputStream is = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MailMessage mailMessage = new MailMessage(mimeMessage, source);

		Assert.assertEquals(mimeMessage.getSubject(), mailMessage
				.getImapMailMessage().getSubject());
		Assert.assertEquals(mimeMessage.getReceivedDate(), mailMessage
				.getImapMailMessage().getMessageDate());

		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
				mailMessage.getImapMailMessage().getFrom().getAddress());
		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
				mailMessage.getImapMailMessage().getFrom().getName());

		Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
				mailMessage.getImapMailMessage().getTo().length);
		if (mimeMessage.getRecipients(RecipientType.CC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.CC).length,
					mailMessage.getImapMailMessage().getCc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getAddress(),
						mailMessage.getImapMailMessage().getCc()[i]
								.getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getPersonal(),
						mailMessage.getImapMailMessage().getCc()[i].getName());
			}
		} else {
			Assert.assertEquals(0,
					mailMessage.getImapMailMessage().getCc().length);
		}

		if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.BCC).length,
					mailMessage.getImapMailMessage().getBcc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getAddress(),
						mailMessage.getImapMailMessage().getBcc()[i]
								.getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getPersonal(),
						mailMessage.getImapMailMessage().getBcc()[i].getName());
			}
		} else {
			Assert.assertEquals(0,
					mailMessage.getImapMailMessage().getBcc().length);
		}

		List<Header> mimeHeaders = Collections
				.list(mimeMessage.getAllHeaders());
		Assert.assertEquals(mimeHeaders.size(), mailMessage
				.getImapMailMessage().getHeaders().length);
		for (int i = 0; i < mimeHeaders.size(); i++) {
			Assert.assertEquals(mimeHeaders.get(i).getName(), mailMessage
					.getImapMailMessage().getHeaders()[i].getName());
			Assert.assertEquals(mimeHeaders.get(i).getValue(), mailMessage
					.getImapMailMessage().getHeaders()[i].getValue());
		}

		MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();
		
		Assert.assertTrue(contents.getBodyPart(0).getContent().toString().equals(mailMessage
				.getImapMailMessage().getBody()));

		Assert.assertTrue(contents.getBodyPart(1).getContent().toString().equals(mailMessage
				.getImapMailMessage().getBodyHTML()));

		Assert.assertEquals(mimeMessage.getMessageID(),
				mailMessage.getMessageId());
	}

}
