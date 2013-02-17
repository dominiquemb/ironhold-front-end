package com.reqo.ironhold.search;

import java.util.List;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.reqo.ironhold.search.model.IndexedMailMessage;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.IndexStatus;
import com.reqo.ironhold.storage.model.LogLevel;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MimeMailMessage;

public class IMAPIndexer {
	static {
		System.setProperty("jobname", IMAPIndexer.class.getSimpleName());
	}
	private static Logger logger = Logger.getLogger(IMAPIndexer.class);

	public static void main(String[] args) {

		IndexerOptions bean = new IndexerOptions();
		CmdLineParser parser = new CmdLineParser(bean);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			return;
		}
		try {
			new IMAPIndexer(bean.getClient(), bean.getBatchSize());
		} catch (Exception e) {
			logger.error("Critical error detected. Exiting.", e);
			System.exit(0);
		}

	}

	public IMAPIndexer(String client, int batchSize) throws Exception {
		final IStorageService storageService = new MongoService(client,
				"indexer");
		final IndexService indexService = new IndexService(client);

		while (true) {
			List<MimeMailMessage> mailMessages = storageService
					.findUnindexedIMAPMessages(batchSize);

			for (final MimeMailMessage mailMessage : mailMessages) {
				logger.info("Indexing " + mailMessage.getMessageId());
				try {
					indexService.store(new IndexedMailMessage(mailMessage));

					logger.info("Message indexed with "
							+ mailMessage.getAttachments().length
							+ " attachments");

					LogMessage logMessage = new LogMessage(LogLevel.Success,
							"Message indexed with "
									+ mailMessage.getAttachments().length
									+ " attachments",
							mailMessage.getMessageId());
					storageService.store(logMessage);

					storageService.updateIndexStatus(mailMessage,
							IndexStatus.INDEXED);

				} catch (Exception e2) {

					try {
						logger.error(
								"Failed to index message "
										+ mailMessage.getMessageId(), e2);

						storageService.updateIndexStatus(mailMessage,
								IndexStatus.FAILED);

						LogMessage logMessage = new LogMessage(
								LogLevel.Failure, "Failed to index message ["
										+ e2.getMessage() + "]",
								mailMessage.getMessageId());
						storageService.store(logMessage);
					} catch (Exception e) {
						logger.error("Critical error detected. Exiting.", e);
						System.exit(0);
					}
				}

			}

			if (mailMessages.size() == 0) {
				Thread.sleep(10000);
			} else {
				logger.info("Indexed " + mailMessages.size() + " messages");
			}
		}
	}

}
