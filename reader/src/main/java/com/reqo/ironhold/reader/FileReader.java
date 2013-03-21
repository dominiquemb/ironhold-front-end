package com.reqo.ironhold.reader;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;

public class FileReader {
	static {
		System.setProperty("jobname", FileReader.class.getSimpleName());
	}
	private static Logger logger = Logger.getLogger(FileReader.class);
	private final IStorageService storageService;
	private String emlFile;

	public FileReader(String client, String emlFile) throws IOException {
		this.emlFile = emlFile;

		this.storageService = new MongoService(client, "FileReader");

	}

	public void processMail() throws InterruptedException, MessagingException, FileNotFoundException {
		File file = new File(emlFile);
		InputStream is = new FileInputStream(file);
		MimeMessage mimeMessage = new MimeMessage(null, is);

		MimeMailMessage mailMessage = null;
		try {
			mailMessage = new MimeMailMessage();

			mailMessage.loadMimeMessage((MimeMessage) mimeMessage, false);

			String messageId = mailMessage.getMessageId();

			if (storageService.existsMimeMailMessage(messageId)) {
				logger.warn("Found duplicate " + messageId);
			} else {
				storageService.store(mailMessage);
			}

		} catch (Exception e) {
			if (mailMessage != null) {
				logger.info(mailMessage.getRawContents());
			}
			logger.error("Failed to process message", e);
		}

	}

	// Main Function for The readEmail Class
	public static void main(String args[]) {
		FileReaderOptions bean = new FileReaderOptions();
		CmdLineParser parser = new CmdLineParser(bean);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			logger.error(e);
			parser.printUsage(System.err);
			return;
		}
		try {
			FileReader readMail = new FileReader(bean.getClient(), bean.getEmlFile());

			try {
				long started = System.currentTimeMillis();
				readMail.processMail();
				long finished = System.currentTimeMillis();
					logger.info("Processed message in " + (finished - started) + "ms");
				
			} catch (InterruptedException e) {
				logger.warn("Got interrupted", e);
			}
		} catch (Exception e) {
			logger.error("Critical error detected, exiting", e);
			System.exit(1);
		}

	}
}
