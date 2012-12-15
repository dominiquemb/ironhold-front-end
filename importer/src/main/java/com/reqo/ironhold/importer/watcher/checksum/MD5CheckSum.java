package com.reqo.ironhold.importer.watcher.checksum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class MD5CheckSum {
	private static final int BUFFER_SIZE = 1024;

	private static Logger logger = Logger.getLogger(MD5CheckSum.class);

	private File checkSumFile;
	private File dataFile;
	private String checkSum;
	private String dataFileName;
	private String commentary;
	private String mailBoxName;
	private String originalFilePath;

	public static File createMD5CheckSum(File dataFile) throws NoSuchAlgorithmException, IOException  {
		String md5 = MD5CheckSum.getMD5Checksum(dataFile);
		logger.info("Generated md5: " + md5 + " for " + dataFile.toString());
		File checkSumFile = new File(dataFile.getParent() + File.separator + FilenameUtils.getBaseName(dataFile.toString()) + ".md5");
		FileWriter fw = new FileWriter(checkSumFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(md5 + " "  + dataFile.getName());
		bw.close();
		
		return checkSumFile;
	}
	public MD5CheckSum(File checkSumFile) throws Exception {
		this.checkSumFile = checkSumFile;

		Path path = Paths.get(checkSumFile.toURI());
		List<String> lines = Files.readAllLines(path, Charset.defaultCharset());

		for (String line : lines) {
			if (!line.startsWith("'")) {
				String[] lineChunks = line.split("\\s+", 2);
				if (lineChunks.length != 2) {
					throw new Exception("Error processing " + checkSumFile
							+ ": '" + line + "' is not in valid format");
				}

				checkSum = lineChunks[0];
				dataFileName = lineChunks[1];

				dataFile = new File(checkSumFile.getParentFile().toString()
						+ File.separator + dataFileName);

			} else {
				if (line.contains("=")) {
					String[] lineChunks = line.substring(1).split("=", 2);
					if (lineChunks.length != 2) {
						throw new Exception("Error processing " + checkSumFile
								+ ": '" + line + "' is not in valid format");
					}
					if (lineChunks[0].trim().equalsIgnoreCase("mailboxname")) {
						mailBoxName = lineChunks[1].trim();
					} else if (lineChunks[0].trim().equalsIgnoreCase("originalfilepath")) {
						originalFilePath = lineChunks[1].trim();
					}
				} else {
					commentary += line.substring(1) + "\n";
				}
			}
		}
	}

	public boolean verifyChecksum() throws NoSuchAlgorithmException, IOException  {
		String actualCheckSum = MD5CheckSum.getMD5Checksum(dataFile);
		logger.info("Actual CheckSum: " + actualCheckSum + " for " + dataFile.toString());
		return checkSum.equals(actualCheckSum);

	}

	private static byte[] createChecksum(File file) throws NoSuchAlgorithmException, IOException  {
		InputStream fis = new FileInputStream(file);

		byte[] buffer = new byte[BUFFER_SIZE];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		return complete.digest();
	}

	// see this How-to for a faster way to convert
	// a byte array to a HEX string
	public static String getMD5Checksum(File file) throws NoSuchAlgorithmException, IOException  {
		byte[] b = createChecksum(file);
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public File getCheckSumFile() {
		return checkSumFile;
	}

	public File getDataFile() {
		return dataFile;
	}

	public String getCheckSum() {
		return checkSum;
	}

	public String getDataFileName() {
		return dataFileName;
	}

	public String getCommentary() {
		return commentary;
	}

	public String getMailBoxName() {
		return mailBoxName;
	}

	public String getOriginalFilePath() {
		return originalFilePath;
	}

}