package com.reqo.ironhold.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.MimeMailMessage;

public class IMAPExporter {
	private static Logger logger = Logger.getLogger(IMAPExporter.class);

	private final String data;
	private final int batchSize;
	private final String client;
	private final IStorageService storageService;
	private final String compression;
	private final int max;

	public IMAPExporter(String data, int batchSize, int max, String client,
			String compression, IStorageService storageService) {
		this.data = data;
		this.batchSize = batchSize;
		this.max = max;
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
			IMAPExporter exporter = new IMAPExporter(bean.getData(),
					bean.getBatchSize(), bean.getMax(), bean.getClient(),
					bean.getCompression(), storageService);
			exporter.start();
		} catch (Exception e) {
			logger.error("Critical error detected. Exiting.", e);
			System.exit(0);
		}
	}

	private void start() {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY");
		Calendar c = GregorianCalendar.getInstance();
		c.set(2000, 1, 1);

		Date date = c.getTime();
		try {
			int count = 0;
			while (true) {

				List<MimeMailMessage> newMessages = storageService
						.findNewMimeMailMessagesSince(date, batchSize);

				if (newMessages.size() > 0 && count < max) {
					logger.info("Exported " + count + " messages");
					for (MimeMailMessage newMessage : newMessages) {
						String dirName = data + File.separator + client
								+ File.separator
								+ sdf.format(newMessage.getMessageDate());
						FileUtils.forceMkdir(new File(dirName));
						String filename = dirName
								+ File.separator
								+ newMessage.getMessageId().replaceAll("\\W+",
										"_");

						compress(new File(filename),
								newMessage.getRawContents());
					}

					date = storageService.getUploadDate(newMessages
							.get(newMessages.size() - 1));
					count += newMessages.size();
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
		if (!compression.equals("NONE")) {
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
		} else {
			FileUtils.writeStringToFile(file, contents);
		}
	}
}
