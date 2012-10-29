package com.reqo.ironhold.search;

import java.util.List;

import org.apache.log4j.Logger;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.MailMessage;

public class Indexer {
	private static Logger logger = Logger.getLogger(Indexer.class);

	public static void main(String[] args) {
		try {
			new Indexer();
		} catch (Exception e) {
			logger.error("Critical error detected. Exiting.", e);
			System.exit(0);
		}
	}

	public Indexer() throws Exception {
		IStorageService storageService = new MongoService("reqo", "indexer");
		IndexService indexService = new IndexService("reqo");

		while (true) {
			List<MailMessage> mailMessages = storageService
			.findUnindexedMessages(10);
			for (MailMessage mailMessage : mailMessages) {
				indexService.store(mailMessage);
				storageService.markAsIndexed(mailMessage.getMessageId());
				logger.info("Indexed " + mailMessage.getMessageId());
			}
			
			if (mailMessages.size() == 0) {
				Thread.sleep(10000);
			}
		}
	}

}
