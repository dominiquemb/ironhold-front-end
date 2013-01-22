package com.reqo.ironhold.storage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.reqo.ironhold.storage.model.IMAPMessageSource;
import com.reqo.ironhold.storage.model.MessageSourceTestModel;
import com.reqo.ironhold.storage.model.MimeMailMessage;

public class EmlLoadTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private IMAPMessageSource source = MessageSourceTestModel
			.generateIMAPMessageSource();

	@Test
	public void testGetRawMessage1() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testMimeMessageWithHTML.eml"));
		InputStream is = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is);
		File messageFile = new File(tempFolder.getRoot() + File.separator
				+ "testGetRawMessage1.eml");
		OutputStream fos = new FileOutputStream(messageFile);

		fos.write(getRawMessage(mimeMessage).getBytes());
		fos.flush();
		fos.close();

		List<String> orioginalLines = Files.readAllLines(
				Paths.get(file.toURI()), Charset.defaultCharset());
		StringBuilder original = new StringBuilder();
		for (String line : orioginalLines) {
			original.append(line + "\n");
		}

		List<String> messageLines = Files.readAllLines(
				Paths.get(messageFile.toURI()), Charset.defaultCharset());
		StringBuilder message = new StringBuilder();
		for (String line : messageLines) {
			message.append(line + "\n");
		}

		Assert.assertEquals(original.toString(), message.toString());

	}

	@Test
	public void testGetRawMessage2() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testMimeMessageWithHTMLandAttachment.eml"));
		InputStream is = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is);
		File messageFile = new File(tempFolder.getRoot() + File.separator
				+ "testGetRawMessage2.eml");
		OutputStream fos = new FileOutputStream(messageFile);

		fos.write(getRawMessage(mimeMessage).getBytes());
		fos.flush();
		fos.close();

		List<String> orioginalLines = Files.readAllLines(
				Paths.get(file.toURI()), Charset.defaultCharset());
		StringBuilder original = new StringBuilder();
		for (String line : orioginalLines) {
			original.append(line + "\n");
		}

		List<String> messageLines = Files.readAllLines(
				Paths.get(messageFile.toURI()), Charset.defaultCharset());
		StringBuilder message = new StringBuilder();
		for (String line : messageLines) {
			message.append(line + "\n");
		}

		Assert.assertEquals(original.toString(), message.toString());

	}

	@Test
	public void testGetRawMessage3() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testMimeMessageWithImage.eml"));
		InputStream is = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is);
		File messageFile = new File(tempFolder.getRoot() + File.separator
				+ "testGetRawMessage3.eml");
		OutputStream fos = new FileOutputStream(messageFile);

		fos.write(getRawMessage(mimeMessage).getBytes());
		fos.flush();
		fos.close();

		List<String> orioginalLines = Files.readAllLines(
				Paths.get(file.toURI()), Charset.defaultCharset());
		StringBuilder original = new StringBuilder();
		for (String line : orioginalLines) {
			original.append(line + "\n");
		}

		List<String> messageLines = Files.readAllLines(
				Paths.get(messageFile.toURI()), Charset.defaultCharset());
		StringBuilder message = new StringBuilder();
		for (String line : messageLines) {
			message.append(line + "\n");
		}

		Assert.assertEquals(original.toString(), message.toString());

	}

	@Test
	public void testMimeMessageWithHTML() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testMimeMessageWithHTML.eml"));
		InputStream is = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.loadMimeMessage(mimeMessage);
		mailMessage.addSource(source);

		Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
		Assert.assertEquals(mimeMessage.getSentDate(),
				mailMessage.getMessageDate());

		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
				mailMessage.getFrom().getAddress());
		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
				mailMessage.getFrom().getName());

		Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
				mailMessage.getTo().length);
		if (mimeMessage.getRecipients(RecipientType.CC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.CC).length,
					mailMessage.getCc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getAddress(),
						mailMessage.getCc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getPersonal(),
						mailMessage.getCc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getCc().length);
		}

		if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.BCC).length,
					mailMessage.getBcc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getAddress(),
						mailMessage.getBcc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getPersonal(),
						mailMessage.getBcc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getBcc().length);
		}

		MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

		Assert.assertEquals(contents.getBodyPart(0).getContent().toString(),
				mailMessage.getBody());

		Assert.assertEquals(contents.getBodyPart(0).getContentType(),
				mailMessage.getBodyContentType());

		Assert.assertEquals(contents.getBodyPart(1).getContent().toString(),
				mailMessage.getBodyHTML());

		Assert.assertEquals(contents.getBodyPart(1).getContentType(),
				mailMessage.getBodyHTMLContentType());

		Assert.assertEquals(mimeMessage.getMessageID(),
				mailMessage.getMessageId());
	}

	@Test
	public void testMimeMessageWithHTMLFromString() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testMimeMessageWithHTML.eml"));
		InputStream is = new FileInputStream(file);

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		InputStream is2 = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is2);

		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.loadMimeMessageFromSource(sb.toString());
		mailMessage.addSource(source);

		Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
		Assert.assertEquals(mimeMessage.getSentDate(),
				mailMessage.getMessageDate());

		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
				mailMessage.getFrom().getAddress());
		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
				mailMessage.getFrom().getName());

		Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
				mailMessage.getTo().length);
		if (mimeMessage.getRecipients(RecipientType.CC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.CC).length,
					mailMessage.getCc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getAddress(),
						mailMessage.getCc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getPersonal(),
						mailMessage.getCc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getCc().length);
		}

		if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.BCC).length,
					mailMessage.getBcc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getAddress(),
						mailMessage.getBcc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getPersonal(),
						mailMessage.getBcc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getBcc().length);
		}

		MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

		Assert.assertEquals(contents.getBodyPart(0).getContent().toString(),
				mailMessage.getBody());

		Assert.assertEquals(contents.getBodyPart(0).getContentType(),
				mailMessage.getBodyContentType());

		Assert.assertEquals(contents.getBodyPart(1).getContent().toString(),
				mailMessage.getBodyHTML());

		Assert.assertEquals(contents.getBodyPart(1).getContentType(),
				mailMessage.getBodyHTMLContentType());

		Assert.assertEquals(mimeMessage.getMessageID(),
				mailMessage.getMessageId());
	}

	private String getRawMessage(MimeMessage mimeMessage)
			throws MessagingException, IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		List<String> lines = Collections.list(mimeMessage.getAllHeaderLines());
		for (String line : lines) {
			os.write((line + "\n").getBytes());
		}
		os.write("\n".getBytes());
		InputStream rawStream = mimeMessage.getRawInputStream();
		int read = 0;
		byte[] bytes = new byte[1024];

		while ((read = rawStream.read(bytes)) != -1) {
			os.write(bytes, 0, read);
		}
		rawStream.close();

		return os.toString();
	}

	@Test
	public void testMimeMessageWithHTMLandAttachment() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testMimeMessageWithHTMLandAttachment.eml"));
		InputStream is = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.loadMimeMessage(mimeMessage);
		mailMessage.addSource(source);

		Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
		Assert.assertEquals(mimeMessage.getSentDate(),
				mailMessage.getMessageDate());

		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
				mailMessage.getFrom().getAddress());
		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
				mailMessage.getFrom().getName());

		Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
				mailMessage.getTo().length);
		if (mimeMessage.getRecipients(RecipientType.CC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.CC).length,
					mailMessage.getCc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getAddress(),
						mailMessage.getCc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getPersonal(),
						mailMessage.getCc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getCc().length);
		}

		if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.BCC).length,
					mailMessage.getBcc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getAddress(),
						mailMessage.getBcc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getPersonal(),
						mailMessage.getBcc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getBcc().length);
		}

		MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

		Multipart part1 = (Multipart) contents.getBodyPart(0).getContent();

		Assert.assertEquals(part1.getBodyPart(0).getContent().toString(),
				mailMessage.getBody());

		Assert.assertEquals(part1.getBodyPart(0).getContentType(),
				mailMessage.getBodyContentType());

		Assert.assertEquals(part1.getBodyPart(1).getContent().toString(),
				mailMessage.getBodyHTML());

		Assert.assertEquals(part1.getBodyPart(1).getContentType(),
				mailMessage.getBodyHTMLContentType());

		InputStream part2 = (InputStream) contents.getBodyPart(1).getContent();
		Assert.assertEquals(contents.getBodyPart(1).getFileName(),
				mailMessage.getAttachments()[0].getFileName());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int bytesRead;
		while ((bytesRead = part2.read(buf)) != -1) {
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

		InputStream inputStream = new ByteArrayInputStream(
				Base64.decodeBase64(mailMessage.getAttachments()[0].getBody()
						.getBytes()));

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

	@Test
	public void testMimeMessageWithImage() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testMimeMessageWithImage.eml"));
		InputStream is = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.loadMimeMessage(mimeMessage);
		mailMessage.addSource(source);

		Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
		Assert.assertEquals(mimeMessage.getSentDate(),
				mailMessage.getMessageDate());

		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
				mailMessage.getFrom().getAddress());
		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
				mailMessage.getFrom().getName());

		Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
				mailMessage.getTo().length);
		if (mimeMessage.getRecipients(RecipientType.CC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.CC).length,
					mailMessage.getCc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getAddress(),
						mailMessage.getCc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getPersonal(),
						mailMessage.getCc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getCc().length);
		}

		if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.BCC).length,
					mailMessage.getBcc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getAddress(),
						mailMessage.getBcc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getPersonal(),
						mailMessage.getBcc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getBcc().length);
		}

		MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

		Multipart part1 = (Multipart) contents.getBodyPart(0).getContent();

		Assert.assertEquals(part1.getBodyPart(0).getContent().toString(),
				mailMessage.getBody());

		Assert.assertEquals(part1.getBodyPart(0).getContentType(),
				mailMessage.getBodyContentType());

		Assert.assertEquals(part1.getBodyPart(1).getContent().toString(),
				mailMessage.getBodyHTML());

		Assert.assertEquals(part1.getBodyPart(1).getContentType(),
				mailMessage.getBodyHTMLContentType());

		InputStream part2 = (InputStream) contents.getBodyPart(1).getContent();
		Assert.assertEquals(contents.getBodyPart(1).getFileName(),
				mailMessage.getAttachments()[0].getFileName());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int bytesRead;
		while ((bytesRead = part2.read(buf)) != -1) {
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

		InputStream inputStream = new ByteArrayInputStream(
				Base64.decodeBase64(mailMessage.getAttachments()[0].getBody()
						.getBytes()));

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

	@Test
	public void testJournalMimeMessage() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testJournalMimeMessage.eml"));
		InputStream is = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.loadMimeMessage(mimeMessage);
		mailMessage.addSource(source);

		Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
		Assert.assertEquals(mimeMessage.getSentDate(),
				mailMessage.getMessageDate());

		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
				mailMessage.getFrom().getAddress());
		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
				mailMessage.getFrom().getName());

		Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
				mailMessage.getTo().length);
		if (mimeMessage.getRecipients(RecipientType.CC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.CC).length,
					mailMessage.getCc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getAddress(),
						mailMessage.getCc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getPersonal(),
						mailMessage.getCc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getCc().length);
		}

		if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.BCC).length,
					mailMessage.getBcc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getAddress(),
						mailMessage.getBcc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getPersonal(),
						mailMessage.getBcc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getBcc().length);
		}

		MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

		MimeMessage internalMessage = (MimeMessage) contents.getBodyPart(0)
				.getContent();

		String internalContents = (String) internalMessage.getContent();

		Assert.assertEquals(internalContents, mailMessage.getBodyHTML());

		Assert.assertEquals(mimeMessage.getMessageID(),
				mailMessage.getMessageId());
	}

	@Test
	public void testInvalidAddress() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testInvalidAddress.eml"));
		InputStream is = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.loadMimeMessage(mimeMessage);
		mailMessage.addSource(source);

		Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
		Assert.assertEquals(mimeMessage.getSentDate(),
				mailMessage.getMessageDate());

		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
				mailMessage.getFrom().getAddress());
		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
				mailMessage.getFrom().getName());

		Assert.assertEquals(1, mailMessage.getTo().length);
		if (mimeMessage.getRecipients(RecipientType.CC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.CC).length,
					mailMessage.getCc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getAddress(),
						mailMessage.getCc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getPersonal(),
						mailMessage.getCc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getCc().length);
		}

		if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.BCC).length,
					mailMessage.getBcc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getAddress(),
						mailMessage.getBcc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getPersonal(),
						mailMessage.getBcc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getBcc().length);
		}

		MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

		MimeMessage internalMessage = (MimeMessage) contents.getBodyPart(1)
				.getContent();

		String internalContents = (String) internalMessage.getContent();

		Assert.assertEquals(internalContents, mailMessage.getBodyHTML());

		Assert.assertEquals(mimeMessage.getMessageID(),
				mailMessage.getMessageId());
	}

	@Test
	public void testMimeMessageWithOnBehalf() throws Exception {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource("/testMimeMessageWithOnBehalf.eml"));
		InputStream is = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.loadMimeMessage(mimeMessage);
		mailMessage.addSource(source);

		Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
		Assert.assertEquals(mimeMessage.getSentDate(),
				mailMessage.getMessageDate());

		Assert.assertEquals(
				((InternetAddress) mimeMessage.getSender()).getAddress(),
				mailMessage.getSender().getAddress());
		Assert.assertEquals(((InternetAddress) mimeMessage.getSender())
				.getPersonal() == null ? StringUtils.EMPTY
				: ((InternetAddress) mimeMessage.getSender()).getPersonal(),
				mailMessage.getSender().getName());
		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
				mailMessage.getFrom().getAddress());
		Assert.assertEquals(((InternetAddress) mimeMessage.getFrom()[0])
				.getPersonal() == null ? StringUtils.EMPTY
				: ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
				mailMessage.getFrom().getName());

		Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
				mailMessage.getTo().length);
		if (mimeMessage.getRecipients(RecipientType.CC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.CC).length,
					mailMessage.getCc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getAddress(),
						mailMessage.getCc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getPersonal(),
						mailMessage.getCc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getCc().length);
		}

		if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.BCC).length,
					mailMessage.getBcc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getAddress(),
						mailMessage.getBcc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getPersonal(),
						mailMessage.getBcc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getBcc().length);
		}

		MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

		MimeMessage embeddedMessage = (MimeMessage) contents.getBodyPart(1)
				.getContent();
		Assert.assertEquals(((MimeMultipart) embeddedMessage.getContent())
				.getBodyPart(1).getContent().toString(),
				mailMessage.getBodyHTML());

		Assert.assertEquals(mimeMessage.getMessageID(),
				mailMessage.getMessageId());
	}

	@Test
	public void testMimeMessageBig() throws Exception {
		performBasicCheckout("/testMimeMessageBig.eml");
	}
	
	@Test
	public void testUnsupportedEncodingException3D() throws Exception {
		performBasicCheckout("/testUnsupportedEncodingException3D.eml");
	}

	@Test
	public void testUnknownEncoding8dashBit() throws Exception {
		performBasicCheckout("/testUnknownEncoding8dashBit.eml");
	}
	
	@Test
	public void testUnknownEncodingQuote() throws Exception {
		performBasicCheckout("/testUnknownEncodingQuote.eml");
	}
	
	@Test
	public void testNullImage() throws Exception {
		performBasicCheckout("/testNullImage.eml");
	}
	
	@Test
	public void testBlankContent() throws Exception {
		performBasicCheckout("/testBlankContent.eml");
	}
	
	private void performBasicCheckout(String fileName) throws MessagingException, IOException {
		File file = FileUtils.toFile(EmlLoadTest.class
				.getResource(fileName));
		InputStream is = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.loadMimeMessage(mimeMessage);
		mailMessage.addSource(source);

		Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
		Assert.assertEquals(mimeMessage.getSentDate(),
				mailMessage.getMessageDate());

		Assert.assertEquals(
				((InternetAddress) mimeMessage.getSender()).getAddress(),
				mailMessage.getSender().getAddress());
		Assert.assertEquals(((InternetAddress) mimeMessage.getSender())
				.getPersonal() == null ? StringUtils.EMPTY
				: ((InternetAddress) mimeMessage.getSender()).getPersonal(),
				mailMessage.getSender().getName());
		Assert.assertEquals(
				((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
				mailMessage.getFrom().getAddress());
		Assert.assertEquals(((InternetAddress) mimeMessage.getFrom()[0])
				.getPersonal() == null ? StringUtils.EMPTY
				: ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
				mailMessage.getFrom().getName());

		Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
				mailMessage.getTo().length);
		if (mimeMessage.getRecipients(RecipientType.CC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.CC).length,
					mailMessage.getCc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getAddress(),
						mailMessage.getCc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.CC)[i]).getPersonal(),
						mailMessage.getCc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getCc().length);
		}

		if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

			Assert.assertEquals(
					mimeMessage.getRecipients(RecipientType.BCC).length,
					mailMessage.getBcc().length);

			for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getAddress(),
						mailMessage.getBcc()[i].getAddress());
				Assert.assertEquals(((InternetAddress) mimeMessage
						.getRecipients(RecipientType.BCC)[i]).getPersonal(),
						mailMessage.getBcc()[i].getName());
			}
		} else {
			Assert.assertEquals(0, mailMessage.getBcc().length);
		}

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

}
