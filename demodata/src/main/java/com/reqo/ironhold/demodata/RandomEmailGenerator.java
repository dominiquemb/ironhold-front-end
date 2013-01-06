package com.reqo.ironhold.demodata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.fluttercode.datafactory.impl.DataFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

public class RandomEmailGenerator {
	protected static DataFactory df = new DataFactory();

	/**
	 * @param args
	 * @throws EmailException
	 * @throws MessagingException
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static void main(String[] args) throws EmailException, IOException,
			MessagingException, DocumentException {
		RandomEmailGenerator reg = new RandomEmailGenerator();
		System.out.println(reg.generate());

	}
	
	private Map<String, String> names = new HashMap<String, String>();

	private RandomTextGenerator rtg;

	public RandomEmailGenerator() {
		this.rtg = new RandomTextGenerator();

		for (int i = 0; i<100; i++)  {
			names.put(df.getEmailAddress().replaceAll(" ", ""), df.getName());
		}
	}

	public String generate() throws EmailException, IOException,
			MessagingException, DocumentException {

		MultiPartEmail email = new MultiPartEmail();
		email.setHostName("abc");
		int toCount = (int) (Math.random() * 5) + 1;
		for (int i = 0; i < toCount; i++) {
			int person = (int) (Math.random()*names.size());
			email.addTo(names.keySet().toArray()[person].toString(), names.values().toArray()[person].toString());
		}

		int ccCount = (int) (Math.random() * 5) + 1;
		for (int i = 0; i < ccCount; i++) {
			int person = (int) (Math.random()*names.size());
			email.addCc(names.keySet().toArray()[person].toString(), names.values().toArray()[person].toString());
		}

		int bccCount = (int) (Math.random() * 5) + 1;
		for (int i = 0; i < bccCount; i++) {
			int person = (int) (Math.random()*names.size());
			email.addBcc(names.keySet().toArray()[person].toString(), names.values().toArray()[person].toString());
		}
		int person = (int) (Math.random()*names.size());
		email.setFrom(names.keySet().toArray()[person].toString(), names.values().toArray()[person].toString());

		String text = rtg.generate();
		int cutoff = Math.min(text.length(), 20);
		while (!text.substring(cutoff, cutoff + 1).equals(" ")) {
			cutoff++;
		}
		email.setSubject(text.substring(0, cutoff).replaceAll("\r?\n", " ") + "...");

		Date randomDate = new Date();
		randomDate.setTime((long) (randomDate.getTime() * Math.random()));

		email.setSentDate(randomDate);

		email.setMsg(text);

		int attachmentFlag = (int) (Math.random() * 3);
		for (int i = 0; i < attachmentFlag; i++) {

			Document document = new Document(PageSize.A4, 50, 50, 50, 50);

			String attachmentName = df.getBusinessName().replace(" ", "");
			File file = new File(attachmentName + ".pdf");
			FileOutputStream attachmentStream = new FileOutputStream(file);
			PdfWriter writer = PdfWriter
					.getInstance(document, attachmentStream);
			document.open();

			document.add(new Paragraph(rtg.generate()));
			document.close();

			EmailAttachment attachment = new EmailAttachment();
			attachment.setPath(file.getAbsolutePath());
			attachment.setDisposition(EmailAttachment.ATTACHMENT);
			attachment.setDescription(attachmentName);
			attachment.setName(attachmentName + ".pdf");
			email.attach(attachment);

		}
		// send the email
		email.buildMimeMessage();

		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		email.getMimeMessage().writeTo(byos);
		return byos.toString();

	}

	private String getRawContents(MimeMessage mimeMessage) throws IOException,
			MessagingException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		List<String> lines = Collections.list(mimeMessage.getAllHeaderLines());
		for (String line : lines) {
			os.write((line + "\n").getBytes());
		}
		os.write("\n".getBytes());
		InputStream rawStream = mimeMessage.getRawInputStream();
		int read = 0;
		byte[] bytes = new byte[4096];

		while ((read = rawStream.read(bytes)) != -1) {
			os.write(bytes, 0, read);
		}
		rawStream.close();

		return os.toString();

	}

}
