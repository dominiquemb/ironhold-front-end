package com.reqo.ironhold.search;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.reqo.ironhold.search.model.IndexedMailMessage;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.IndexStatus;
import com.reqo.ironhold.storage.model.LogLevel;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MailMessage;

public class Indexer {
	private static Logger logger = Logger.getLogger(Indexer.class);

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
			new Indexer(bean.getClient(), bean.getBatchSize(), bean.getThreads());
		} catch (Exception e) {
			logger.error("Critical error detected. Exiting.", e);
			System.exit(0);
		}

	}

	public Indexer(String client, int batchSize, int threads) throws Exception {
		final IStorageService storageService = new MongoService(client,
				"indexer");
		final IndexService indexService = new IndexService(client);
		while (true) {
			List<MailMessage> mailMessages = storageService
					.findUnindexedMessages(batchSize);
			ExecutorService executorService = Executors.newFixedThreadPool(threads);
			
			for (final MailMessage mailMessage : mailMessages) {
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						try {
							try {
								indexService.store(new IndexedMailMessage(
										mailMessage));

							} catch (MapperParsingException e) {
								logger.warn("Failed to index message "
										+ mailMessage.getMessageId()
										+ " with attachments, "
										+ "skipping attachments", e);

								LogMessage logMessage = new LogMessage(
										LogLevel.Warning,
										"Failed to index message with attachments, "
												+ "skiping attachments ["
												+ e.getDetailedMessage() + "]",
										mailMessage.getMessageId());
								storageService.log(logMessage);
								mailMessage.removeAttachments();

								indexService.store(new IndexedMailMessage(
										mailMessage));
							}

							LogMessage logMessage = new LogMessage(
									LogLevel.Success,
									"Message indexed with "
											+ mailMessage.getAttachments().length
											+ " attachments", mailMessage
											.getMessageId());
							storageService.log(logMessage);

							storageService.updateIndexStatus(
									mailMessage.getMessageId(),
									IndexStatus.INDEXED);
						} catch (Exception e2) {

							try {
								logger.error("Failed to index message "
										+ mailMessage.getMessageId(), e2);

								storageService.updateIndexStatus(
										mailMessage.getMessageId(),
										IndexStatus.FAILED);

								LogMessage logMessage = new LogMessage(
										LogLevel.Failure,
										"Failed to index message ["
												+ e2.getMessage() + "]",
										mailMessage.getMessageId());
								storageService.log(logMessage);
							} catch (Exception e) {
								logger.error(
										"Critical error detected. Exiting.", e);
								System.exit(0);
							}
						}

					}
				});
			}
			
			executorService.shutdown();
			while (!executorService.isTerminated()) {
				executorService.awaitTermination(1, TimeUnit.SECONDS);
			}
			
			if (mailMessages.size() == 0) {
				Thread.sleep(10000);
			} else {
				logger.info("Indexed " + mailMessages.size() + " messages");
			}
		}
	}

}
