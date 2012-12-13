package com.reqo.ironhold.reader;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.ReadOnlyFolderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;

public class IMAPReader {

	private static Logger logger = Logger.getLogger(IMAPReader.class);

	public IMAPReader() {
	}

	private void printData(String data) {
	//	System.out.println(data);
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
			store = session.getStore("imaps");

			store.connect("72.0.226.101", 993, "TWF\\Journal", "J0urn@l!");

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
			int withAttachments = 0;
			int withoutAttachments = 0;
			// Loop over all of the messages
			for (int messageNumber = 0; messageNumber < messages.length; messageNumber++) {
				// Retrieve the next message to be read
				if (messageNumber % 100 == 0) {
					logger.info("Processed " + messageNumber + " messages");
				}
				message = messages[messageNumber];

				// Retrieve the message content
				messagecontentObject = message.getContent();

				// Determine email type
				if (messagecontentObject instanceof Multipart) {
					withAttachments++;
					
					
				/*	logger.info("Found Email with Attachment");
					sender = ((InternetAddress) message.getFrom()[0])
							.getPersonal();

					// If the "personal" information has no entry, check the
					// address for the sender information
					printData("If the personal information has no entry, check the address for the sender information.");

					if (sender == null) {
						sender = ((InternetAddress) message.getFrom()[0])
								.getAddress();
						printData("sender in NULL. Printing Address:" + sender);
					}
					printData("Sender -." + sender);

					// Get the subject information
					subject = message.getSubject();

					printData("subject=" + subject);

					// Retrieve the Multipart object from the message
					multipart = (Multipart) message.getContent();

					printData("Retrieve the Multipart object from the message");

					// Loop over the parts of the email
					for (int i = 0; i < multipart.getCount(); i++) {
						// Retrieve the next part
						part = multipart.getBodyPart(i);

						// Get the content type
						contentType = part.getContentType();

						// Display the content type
						printData("Content: " + contentType);

						if (contentType.startsWith("text/plain")) {
							printData("---------reading content type text/plain  mail -------------");
						} else {
							// Retrieve the file name
							String fileName = part.getFileName();
							printData("retrive the fileName=" + fileName);
						}
					}*/
				} else {
					withoutAttachments++;
					/*logger.info("Found Mail Without Attachment");
					sender = ((InternetAddress) message.getFrom()[0])
							.getPersonal();

					// If the "personal" information has no entry, check the
					// address for the sender information
					printData("If the personal information has no entry, check the address for the sender information.");

					if (sender == null) {
						sender = ((InternetAddress) message.getFrom()[0])
								.getAddress();
						printData("sender in NULL. Printing Address:" + sender);
					}

					// Get the subject information
					subject = message.getSubject();
					printData("subject=" + subject);*/
				}
				
			
			}

			// Close the folder
			folder.close(true);

			logger.info(String.format("With attachments: %d\nWithout attachments: %d", withAttachments, withoutAttachments));
			// Close the message store
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
		IMAPReader readMail = new IMAPReader();

		// Calling processMail Function to read from IMAP Account
		readMail.processMail();
	}

}
