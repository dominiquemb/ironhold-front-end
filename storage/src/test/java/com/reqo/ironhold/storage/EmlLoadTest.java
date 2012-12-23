package com.reqo.ironhold.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import javax.mail.Header;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.fluttercode.datafactory.impl.DataFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.reqo.ironhold.storage.model.IMAPMessageSource;
import com.reqo.ironhold.storage.model.MailMessage;

public class EmlLoadTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

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
	public void testMimeMessageWithHTML() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testMimeMessageWithHTML.eml"));
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

		Assert.assertEquals(contents.getBodyPart(0).getContent().toString(),
				mailMessage.getImapMailMessage().getBody());

		Assert.assertEquals(contents.getBodyPart(0).getContentType(),
				mailMessage.getImapMailMessage().getBodyContentType());

		Assert.assertEquals(contents.getBodyPart(1).getContent().toString(),
				mailMessage.getImapMailMessage().getBodyHTML());

		Assert.assertEquals(contents.getBodyPart(1).getContentType(),
				mailMessage.getImapMailMessage().getBodyHTMLContentType());

		Assert.assertEquals(mimeMessage.getMessageID(),
				mailMessage.getMessageId());
	}

	@Test
	public void testMimeMessageWithHTMLandAttachment() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testMimeMessageWithHTMLandAttachment.eml"));
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

		Multipart part1 = (Multipart) contents.getBodyPart(0).getContent();

		Assert.assertEquals(part1.getBodyPart(0).getContent().toString(),
				mailMessage.getImapMailMessage().getBody());

		Assert.assertEquals(part1.getBodyPart(0).getContentType(), mailMessage
				.getImapMailMessage().getBodyContentType());

		Assert.assertEquals(part1.getBodyPart(1).getContent().toString(),
				mailMessage.getImapMailMessage().getBodyHTML());

		Assert.assertEquals(part1.getBodyPart(1).getContentType(), mailMessage
				.getImapMailMessage().getBodyHTMLContentType());

		InputStream part2 = (InputStream) contents.getBodyPart(1).getContent();
		Assert.assertEquals(contents.getBodyPart(1).getFileName(),
				mailMessage.getAttachments()[0].getFileName());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// 8176 is the block size used internally and should give the
		// best
		// performance

		byte[] buf = new byte[4096];
		int bytesRead;
		while((bytesRead = part2.read(buf))!=-1) {
		    out.write(buf, 0, bytesRead);
		}
		out.close();

		Assert.assertEquals(Base64.encodeBase64String(out.toByteArray()),
				mailMessage.getAttachments()[0].getBody());

		File attachment = FileUtils.toFile(EmlLoadTest.class.getResource("/"
				+ mailMessage.getAttachments()[0].getFileName()));

		String md5fromFile = getMD5Checksum(attachment);
		OutputStream fos = new FileOutputStream(tempFolder.getRoot()
				+ File.separator
				+ mailMessage.getAttachments()[0].getFileName());
		
		InputStream inputStream = new ByteArrayInputStream(Base64.decodeBase64(mailMessage.getAttachments()[0].getBody().getBytes()));
		
		int read = 0;
		int total = read;
		byte[] bytes = new byte[1024];
	 
		while ((read = inputStream.read(bytes)) != -1) {
			
			total += read;
			fos.write(bytes, 0, read);
		}
		inputStream.close();
		fos.flush();
		fos.close();
		
		
		String md5fromAttachment = getMD5Checksum(new File(tempFolder.getRoot()
				+ File.separator
				+ mailMessage.getAttachments()[0].getFileName()));

		Assert.assertEquals(md5fromFile, md5fromAttachment);

		Assert.assertEquals(mimeMessage.getMessageID(),
				mailMessage.getMessageId());
	}
	
	private static byte[] createChecksum(byte[] bytes)
			throws NoSuchAlgorithmException, IOException {

		InputStream fis = new ByteArrayInputStream(bytes);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		return complete.digest();
	}

	private static byte[] createChecksum(File file)
			throws NoSuchAlgorithmException, IOException {
		InputStream fis = new FileInputStream(file);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		return complete.digest();
	}

	// see this How-to for a faster way to convert
	// a byte array to a HEX string
	public static String getMD5Checksum(File file)
			throws NoSuchAlgorithmException, IOException {
		byte[] b = createChecksum(file);
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	// see this How-to for a faster way to convert
	// a byte array to a HEX string
	public static String getMD5Checksum(byte[] bytes)
			throws NoSuchAlgorithmException, IOException {
		byte[] b = createChecksum(bytes);
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

}
