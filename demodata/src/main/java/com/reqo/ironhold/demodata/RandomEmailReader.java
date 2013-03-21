package com.reqo.ironhold.demodata;

import com.mongodb.MongoException;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.log.LogLevel;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.Date;

public class RandomEmailReader {
	static {
		System.setProperty("jobname", RandomEmailReader.class.getSimpleName());
	}
	/**
	 * @param args
	 * @throws Exception
	 * @throws MongoException
	 */
	public static void main(String[] args) throws MongoException, Exception {
		Options bean = new Options();
		CmdLineParser parser = new CmdLineParser(bean);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			return;
		}
		
		RandomEmailGenerator reg = new RandomEmailGenerator();
		MongoService storageService = new MongoService(bean.getClient(),
				"RandomEmailReader");

		for (int i = 0; i < bean.getNumber(); i++) {
			MimeMailMessage mailMessage = new MimeMailMessage();

			final IMAPMessageSource source = new IMAPMessageSource();

			source.setImapPort(993);
			source.setUsername("leo");
			source.setImapSource("warandpeace.org");
			source.setProtocol("imaps");
			source.setLoadTimestamp(new Date());

			mailMessage.loadMimeMessageFromSource(reg.generate());
			mailMessage.addSource(source);

			String messageId = mailMessage.getMessageId();

			if (storageService.existsMimeMailMessage(messageId)) {
				storageService.addSource(messageId, source);
			} else {
				storageService.store(mailMessage);
				LogMessage logMessage = new LogMessage(LogLevel.Success,
						mailMessage.getMessageId(),
						"Stored journaled message from " + source.getProtocol()
								+ "://" + source.getImapSource() + ":"
								+ source.getImapPort());
				storageService.store(logMessage);
				
			}

			
		}

	}

}
