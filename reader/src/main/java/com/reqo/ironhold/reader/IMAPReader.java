package com.reqo.ironhold.reader;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.ReadOnlyFolderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.IMAPBatchMeta;
import com.reqo.ironhold.storage.model.IMAPMessageSource;
import com.reqo.ironhold.storage.model.LogLevel;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MimeMailMessage;

public class IMAPReader {

	private static Logger logger = Logger.getLogger(IMAPReader.class);
	private final IStorageService storageService;
	private String hostname;
	private int port;
	private String username;
	private String password;
	private String protocol;
	private int batchSize;
	private boolean expunge;

	public IMAPReader(String hostname, int port, String username,
			String password, String protocol, String client, int batchSize,
			boolean expunge) throws IOException {
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
		this.protocol = protocol;
		this.batchSize = batchSize;
		this.expunge = expunge;

		this.storageService = new MongoService(client, "IMAPReader");

	}

	public int processMail() throws InterruptedException {
		Session session = null;
		Store store = null;
		Folder folder = null;
		int messageNumber = 0;
		final IMAPMessageSource source = new IMAPMessageSource();

		source.setImapPort(port);
		source.setUsername(username);
		source.setImapSource(hostname);
		source.setImapPort(port);
		source.setProtocol(protocol);

		final IMAPBatchMeta metaData = new IMAPBatchMeta(source, new Date());

		try {
			logger.info("Journal IMAP Reader started");
			session = Session.getDefaultInstance(System.getProperties(), null);

			logger.info("Getting the session for accessing email.");
			store = session.getStore(protocol);

			store.connect(hostname, port, username, password);

			logger.info("Connection established with IMAP server.");

			// Get a handle on the default folder
			folder = store.getDefaultFolder();

			logger.info("Getting the Inbox folder.");

			// Retrieve the "Inbox"
			folder = folder.getFolder("inbox");

			// Reading the Email Index in Read / Write Mode
			folder.open(Folder.READ_WRITE);

			// Retrieve the messages
			final Message[] messages = folder.getMessages();

			logger.info("Found " + messages.length + " messages");

			// Loop over all of the messages

			for (messageNumber = 0; messageNumber < Math.min(batchSize,
					messages.length); messageNumber++) {
				logger.info("Starting to process message " + messageNumber);
				MimeMailMessage mailMessage = null;
				String messageId = null; 
				try {
					final int currentMessageNumber = messageNumber;
					// Retrieve the next message to be read
					final Message message = messages[currentMessageNumber];
					if (!message.getFlags().contains(Flag.DELETED)) {
						mailMessage = new MimeMailMessage();

						source.setLoadTimestamp(new Date());
						mailMessage.loadMimeMessage((MimeMessage) message,
								false);
						mailMessage.addSource(source);

						 messageId = mailMessage.getMessageId();

						if (storageService.existsMimeMailMessage(messageId)) {
							logger.warn("Found duplicate " + messageId);
							metaData.incrementDuplicates();
							storageService.addSource(messageId, source);
						} else {
							long storedSize = storageService.store(mailMessage);

							metaData.incrementBatchSize(storedSize);
							
							LogMessage logMessage = new LogMessage(
									LogLevel.Success,
									mailMessage.getMessageId(),
									"Stored journaled message from "
											+ source.getProtocol() + "://"
											+ source.getImapSource() + ":"
											+ source.getImapPort());
							storageService.store(logMessage);

							logger.info("Stored journaled message["
									+ currentMessageNumber
									+ "] "
									+ mailMessage.getMessageId()
									+ " "
									+ FileUtils
											.byteCountToDisplaySize(mailMessage
													.getSize()));

							metaData.updateSizeStatistics(mailMessage
									.getRawContents().length(), storedSize);

						}

						metaData.incrementAttachmentStatistics(mailMessage
								.isHasAttachments());
						if (expunge) {
							message.setFlag(Flag.DELETED, true);

						}
						metaData.incrementMessages();
					} else {
						logger.info("Skipping message that was marked deleted ["
								+ messageNumber + "]");
					}

				} catch (Exception e) {
					metaData.incrementFailures();
					if (mailMessage != null) {
						File f = new File(mailMessage.getMessageId() + ".eml");
						if (!f.exists()) {
							FileUtils.writeStringToFile(f,
									mailMessage.getRawContents());
						}
						
						logger.error("Failed to process message " + mailMessage.getMessageId(), e);
					}
					
					e.printStackTrace();

				}

			}

			metaData.setFinished(new Date());
			storageService.addIMAPBatch(metaData);
			// Close the folder
			folder.close(true);

			store.close();
		} catch (AuthenticationFailedException e) {
			logger.error("Not able to process the mail reading.", e);
			e.printStackTrace();
			System.exit(1);
		} catch (FolderClosedException e) {
			logger.error("Not able to process the mail reading.", e);
			e.printStackTrace();
			System.exit(1);
		} catch (FolderNotFoundException e) {
			logger.error("Not able to process the mail reading.", e);
			e.printStackTrace();
			System.exit(1);
		} catch (ReadOnlyFolderException e) {
			logger.error("Not able to process the mail reading.", e);
			e.printStackTrace();
			System.exit(1);
		} catch (StoreClosedException e) {
			logger.error("Not able to process the mail reading.", e);
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			logger.error("Not able to process the mail reading.", e);
			e.printStackTrace();
			System.exit(1);
		}

		return messageNumber;
	}

	// Main Function for The readEmail Class
	public static void main(String args[]) {
		ReaderOptions bean = new ReaderOptions();
		CmdLineParser parser = new CmdLineParser(bean);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			return;
		}
		try {
			IMAPReader readMail = new IMAPReader(bean.getHostname(),
					bean.getPort(), bean.getUsername(), bean.getPassword(),
					bean.getProtocol(), bean.getClient(), bean.getBatchSize(),
					bean.getExpunge());

			// Calling processMail Function to read from IMAP Account
			try {
				while (true) {

					long started = System.currentTimeMillis();
					int number = readMail.processMail();
					long finished = System.currentTimeMillis();
					logger.info("Processed batch with " + number
							+ " messages in " + (finished - started) + "ms");
					if (number < bean.getBatchSize()) {

						Thread.sleep(60000);

					}

				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			logger.error("Critical error detected, exiting", e);
			e.printStackTrace();
			System.exit(1);
		}

	}
}
