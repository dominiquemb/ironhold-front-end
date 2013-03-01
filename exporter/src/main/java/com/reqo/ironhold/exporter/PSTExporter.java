package com.reqo.ironhold.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.MailMessage;

public class PSTExporter {
	static {
		System.setProperty("jobname", PSTExporter.class.getSimpleName());
	}

	private static Logger logger = Logger.getLogger(PSTExporter.class);

	private final String data;
	private final int batchSize;
	private final String client;
	private final String compression;
	private final IStorageService storageService;

	public PSTExporter(String data, int batchSize, String client, String compression,
			IStorageService storageService) {
		this.data = data;
		this.batchSize = batchSize;
		this.client = client;
		this.compression = compression;
		this.storageService = storageService;

	}

	public static void main(String[] args) {
		Options bean = new Options();
		CmdLineParser parser = new CmdLineParser(bean);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			return;
		}
		try {
			IStorageService storageService = new MongoService(bean.getClient(),
					"PSTExporter");
			PSTExporter exporter = new PSTExporter(bean.getData(),
					bean.getBatchSize(), bean.getClient(), bean.getCompression(), storageService);
			exporter.start();
		} catch (Exception e) {
			logger.error("Critical error detected. Exiting.", e);
			System.exit(0);
		}
	}

	private void start() {
		Calendar c = GregorianCalendar.getInstance();
		c.set(2000, 1, 1);
		Date date = c.getTime();
		try {
			while (true) {

				List<MailMessage> newMessages = storageService
						.findNewMailMessagesSince(date, batchSize);

				if (newMessages.size() > 0) {
					logger.info("Exporting " + newMessages.size() + " messages");
					for (MailMessage newMessage : newMessages) {
						String filename = data
								+ File.separator
								+ client
								+ File.separator
								+ newMessage.getMessageId().replaceAll("\\W+",
										"_");
						logger.info("Writing to " + filename);
						compress(new File(filename),
								MailMessage.serializeMailMessage(newMessage));
					}

					date = storageService.getUploadDate(newMessages
							.get(newMessages.size() - 1));
				} else {
					System.exit(1);
				}
			}
		} catch (Exception e) {
			logger.warn(e);
		}

	}

	private void compress(File file, String contents)
			throws CompressorException, IOException, InterruptedException {

		CompressorOutputStream compressedStream = null;
		try {
			compressedStream = new CompressorStreamFactory()
					.createCompressorOutputStream(compression,
							new FileOutputStream(file));

			compressedStream.write(contents.getBytes());
		} finally {
			if (compressedStream != null)
				compressedStream.close();
		}
	}
}
