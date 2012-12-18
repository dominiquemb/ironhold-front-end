package com.reqo.ironhold.reader;

import java.io.IOException;
import java.util.Enumeration;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.ReadOnlyFolderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.IMAPMessageSource;
import com.reqo.ironhold.storage.model.MailMessage;
import com.sun.mail.imap.IMAPNestedMessage;

public class JournalIMAPReader {

	private static Logger logger = Logger.getLogger(JournalIMAPReader.class);
	private final IStorageService storageService;
	private final String client;
	private String hostname;
	private int port;
	private String username;
	private String password;
	private String protocol;

	public JournalIMAPReader(String hostname, int port, String username,
			String password, String protocol, String client) throws IOException {
		this.client = client;
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
		this.protocol = protocol;

		this.storageService = new MongoService(client, "journalImapReader");

	}

	private void printData(String data) {
		System.out.println(data);
	}

	public void processMail() {
		Session session = null;
		Store store = null;
		Folder folder = null;
		Message message = null;
		Message[] messages = null;
		Object messagecontentObject = null;
		String sender = null;
		String subject = null;
		Multipart multipart = null;
		Part part = null;
		String contentType = null;

		try {
			logger.info("--------------processing mails started-----------------");
			session = Session.getDefaultInstance(System.getProperties(), null);

			logger.info("getting the session for accessing email.");
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
			messages = folder.getMessages();

			logger.info("Found " + messages.length + " messages");
			// Loop over all of the messages
			for (int messageNumber = 0; messageNumber < 1000; messageNumber++) {
				// Retrieve the next message to be read
				message = messages[messageNumber];

				IMAPNestedMessage  nestedMessage = null;
				IMAPMessageSource source = new IMAPMessageSource();

				source.setImapPort(port);
				source.setUsername(username);
				source.setImapSource(hostname);
				source.setImapPort(port);

				// Retrieve the message content
				messagecontentObject = message.getContent();

				// Determine email type
				if (messagecontentObject instanceof Multipart) {

					sender = ((InternetAddress) message.getFrom()[0])
							.getPersonal();

					Enumeration<Header> headers = message.getAllHeaders();

					while (headers.hasMoreElements()) {
						Header header = headers.nextElement();
						source.addHeader(header);

					}

					// Retrieve the Multipart object from the message
					multipart = (Multipart) message.getContent();

					// Loop over the parts of the email
					for (int i = 0; i < multipart.getCount(); i++) {
						// Retrieve the next part
						part = multipart.getBodyPart(i);

						// Get the content type
						contentType = part.getContentType();

						// Display the content type
						printData("Content: " + contentType);

						if (contentType.startsWith("message/rfc822")) {
							nestedMessage = (IMAPNestedMessage) part
									.getContent();
							
						}
					}
				} else {
					throw new Exception(
							"This reader supports journal messages only");
				}

				if (nestedMessage == null) {
					throw new Exception(
							"This reader supports journal messages only, failed to find an IMAPNestedMessage");
				}
				MailMessage mailMessage = new MailMessage(nestedMessage, source);
				storageService.store(mailMessage);

			}

			// Close the folder
			folder.close(true);

			store.close();
		} catch (AuthenticationFailedException e) {
			printData("Not able to process the mail reading.");
			e.printStackTrace();
		} catch (FolderClosedException e) {
			printData("Not able to process the mail reading.");
			e.printStackTrace();
		} catch (FolderNotFoundException e) {
			printData("Not able to process the mail reading.");
			e.printStackTrace();
		} catch (ReadOnlyFolderException e) {
			printData("Not able to process the mail reading.");
			e.printStackTrace();
		} catch (StoreClosedException e) {
			printData("Not able to process the mail reading.");
			e.printStackTrace();
		} catch (Exception e) {
			printData("Not able to process the mail reading.");
			e.printStackTrace();
		}
	}

	// Main Function for The readEmail Class
	public static void main(String args[]) {
		// Creating new readEmail Object
		JournalIMAPReader readMail;
		try {
			readMail = new JournalIMAPReader("72.0.226.101", 993,
					"TWF\\Journal", "J0urn@l!", "imaps", "reqo");

			// Calling processMail Function to read from IMAP Account
			readMail.processMail();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
