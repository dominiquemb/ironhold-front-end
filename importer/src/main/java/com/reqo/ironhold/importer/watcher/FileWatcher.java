package com.reqo.ironhold.importer.watcher;

import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public abstract class FileWatcher {
	private static Logger logger = Logger.getLogger(FileWatcher.class);

	private final String inputDirName;
	private final String outputDirName;
	private final String client;

	protected void processFileWrapper(String dataFileName,
			MD5CheckSum checksum) throws Exception {

		File dataFile = new File(getInputDirName(), dataFileName);
		
		processFile(dataFile, checksum);

		if (!dataFile.renameTo(new File(getOutputDirName(), dataFile.getName()))) {
			logger.warn("Failed to move file " + dataFile.toString() + " to " + new File(getOutputDirName(), dataFile.getName()));	
		}
		if (!checksum.getCheckSumFile().renameTo(new File(getOutputDirName(), checksum.getCheckSumFile().getName()))) {
			logger.warn("Failed to move file " + checksum.getCheckSumFile().toString());
		}

		
		

	}

	protected abstract void processFile(File dataFile, MD5CheckSum md5File)
			throws Exception;

	public FileWatcher(String inputDirName, String outputDirName, String client)
			throws Exception {
		this.inputDirName = inputDirName;
		this.outputDirName = outputDirName;
		this.client = client;

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

					MD5CheckSum checkSum = processChecksumFile(inputDirName,
							fileName);
					System.out.println(checkSum.toString());
					processFileWrapper(checkSum.getDataFileName(), checkSum);
					new File(inputDirName, checkSum.getDataFileName()).renameTo(new File(
							outputDirName, checkSum.getDataFileName()));

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
			logger.error("Checksum check failed for " + fileName);
			throw new Exception("Checksum check failed for " + fileName);
		}

		return checkSum;
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

}
