package com.reqo.ironhold.importer.watcher;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;

import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;

public abstract class FileWatcher {
	private static Logger logger = Logger.getLogger(FileWatcher.class);

	private final String inputDirName;
	private final String outputDirName;
	private final String quarantineDirName;
	private final String client;

	private String hostname;

	protected void processFileWrapper(String dataFileName, MD5CheckSum checksum)
			throws Exception {

		File dataFile = new File(getInputDirName(), dataFileName);

		processFile(dataFile, checksum);

		if (!dataFile
				.renameTo(new File(getOutputDirName(), dataFile.getName()))) {
			logger.warn("Failed to move file " + dataFile.toString() + " to "
					+ new File(getOutputDirName(), dataFile.getName()));
		}
		if (!checksum.getCheckSumFile().renameTo(
				new File(getOutputDirName(), checksum.getCheckSumFile()
						.getName()))) {
			logger.warn("Failed to move file "
					+ checksum.getCheckSumFile().toString());
		}

	}

	protected abstract void processFile(File dataFile, MD5CheckSum md5File)
			throws Exception;

	public FileWatcher(String inputDirName, String outputDirName,
			String quarantineDirName, String client) throws Exception {
		this.inputDirName = inputDirName;
		this.outputDirName = outputDirName;
		this.quarantineDirName = quarantineDirName;
		this.client = client;

		InetAddress addr = InetAddress.getLocalHost();

		hostname = addr.getHostName();

		logger.info("Watching " + inputDirName + " directory for " + client);
		Path inputDir = Paths.get(inputDirName);
		WatchService watchService = inputDir.getFileSystem().newWatchService();
		inputDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

		while (true) {
			WatchKey watckKey;
			try {
				watckKey = watchService.take();
			} catch (InterruptedException e) {
				return;
			}

			for (WatchEvent<?> event : watckKey.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();
				if (kind == StandardWatchEventKinds.OVERFLOW) {
					continue;
				}

				String fileName = event.context().toString();
				logger.info("Detected new file: " + fileName);

				if (fileName.endsWith(".md5")) {
					logger.info("Detected md5 file: " + fileName);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						return;
					}

					MD5CheckSum checkSum = null;
					try {
						checkSum = processChecksumFile(inputDirName, fileName);
						if (checkSum != null) {

							processFileWrapper(checkSum.getDataFileName(),
									checkSum);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}

			boolean valid = watckKey.reset();
			if (!valid) {
				break;
			}
		}
	}

	private MD5CheckSum processChecksumFile(String inputDirName, String fileName)
			throws Exception {

		MD5CheckSum checkSum = new MD5CheckSum(new File(getInputDirName(),
				fileName));

		if (!checkSum.verifyChecksum()) {
			logger.warn("Checksum check failed for " + fileName);
			quarantine(checkSum, "Checksum check failed for " + fileName);
			return null;
		}

		return checkSum;
	}

	private void quarantine(MD5CheckSum checkSum, String reason) {
		if (checkSum.getDataFileName() != null) {
			logger.warn("Quarantining file " + checkSum.getDataFileName()
					+ ": " + reason);

			File dataFile = new File(getInputDirName(),
					checkSum.getDataFileName());

			if (!dataFile.renameTo(new File(getQuarantineDirName(), dataFile
					.getName()))) {
				logger.warn("Failed to quarantine file " + dataFile.toString()
						+ " to "
						+ new File(getOutputDirName(), dataFile.getName()));
			}
		}
		if (!checkSum.getCheckSumFile().renameTo(
				new File(getQuarantineDirName(), checkSum.getCheckSumFile()
						.getName()))) {
			logger.warn("Failed to quarantine file "
					+ checkSum.getCheckSumFile().toString());
		}

		send("Quarantining file " + checkSum.getCheckSumFile().toString()
				+ ": " + reason);
	}

	public void send(String subject)  {
		try {
			HtmlEmail email = new HtmlEmail();
			email.setHostName("10.65.0.78");
			email.addTo("admins@ironhold.net", "admins@ironhold.net");
			email.setFrom("admins@ironhold.net", hostname);
			email.setSubject(subject);

			// set the html message
			email.setHtmlMsg(subject);

			// set the alternative message
			email.setTextMsg("Your email client does not support HTML messages");

			// send the email
			email.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getInputDirName() {
		return inputDirName;
	}

	public String getOutputDirName() {
		return outputDirName;
	}

	public String getClient() {
		return client;
	}

	public String getQuarantineDirName() {
		return quarantineDirName;
	}

}
