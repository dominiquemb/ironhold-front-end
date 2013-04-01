package com.reqo.ironhold.demodata;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.fluttercode.datafactory.impl.DataFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class RandomEmailGenerator {
	static {
		System.setProperty("jobname",
				RandomEmailGenerator.class.getSimpleName());
	}
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

		for (int i = 0; i < 100; i++) {
			names.put(df.getEmailAddress().replaceAll(" ", ""), df.getName());
		}
	}

	public String generate() throws EmailException, IOException,
			MessagingException, DocumentException {

		HtmlEmail email = new HtmlEmail();
		email.setHostName("abc");
		int toCount = (int) (Math.random() * 5) + 1;
		for (int i = 0; i < toCount; i++) {
			int person = (int) (Math.random() * names.size());
			email.addTo(names.keySet().toArray()[person].toString(), names
					.values().toArray()[person].toString());
		}

		int ccCount = (int) (Math.random() * 5) + 1;
		for (int i = 0; i < ccCount; i++) {
			int person = (int) (Math.random() * names.size());
			email.addCc(names.keySet().toArray()[person].toString(), names
					.values().toArray()[person].toString());
		}

		int bccCount = (int) (Math.random() * 5) + 1;
		for (int i = 0; i < bccCount; i++) {
			int person = (int) (Math.random() * names.size());
			email.addBcc(names.keySet().toArray()[person].toString(), names
					.values().toArray()[person].toString());
		}
		int person = (int) (Math.random() * names.size());
		email.setFrom(names.keySet().toArray()[person].toString(), names
				.values().toArray()[person].toString());

		String text = rtg.generate();
		int cutoff = Math.min(text.length(), 20);
		while (!text.substring(cutoff, cutoff + 1).equals(" ") || cutoff < 40) {
			cutoff++;
		}
		email.setSubject(text.substring(0, cutoff).replaceAll("\r?\n", " ")
				+ "...");


        int year = randBetween(2000, 2010);

        int month = randBetween(0, 11);

        GregorianCalendar gc = new GregorianCalendar(year, month, 1);

        int day = randBetween(1, gc.getActualMaximum(Calendar.DAY_OF_MONTH));

        gc.set(year, month, day);

		email.setSentDate(gc.getTime());

		email.setMsg(text);
        email.setHtmlMsg("<pre>" + text + "</pre>");

		int attachmentFlag = (int) (Math.random() * 3);
		List<File> toBeDeleted = new ArrayList<File>();
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

			toBeDeleted.add(file);

		}
		// send the email
		email.buildMimeMessage();

		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		email.getMimeMessage().writeTo(byos);

		for (File f : toBeDeleted) {
			f.delete();
		}
		return byos.toString();

	}

	private static int randBetween(int start, int end) {
		return start + (int) Math.round(Math.random() * (end - start));
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
