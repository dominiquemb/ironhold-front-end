package com.reqo.ironhold.watcher;

import com.reqo.ironhold.watcher.checksum.MD5CheckSum;
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

    protected void processFiles(List<String> dataFileNames, String checksumFileName) throws Exception {

        for (String dataFileName : dataFileNames) {
            File dataFile = new File(getInputDirName(), dataFileName);

            processFile(dataFile);

            dataFile.renameTo(new File(getOutputDirName(), dataFileName));
        }

        File checksumFile = new File(getInputDirName(), checksumFileName);
        logger.info("Moving checksum file " + checksumFile.toString());
        checksumFile.renameTo(new File(getOutputDirName(), checksumFileName));
    }

    protected abstract void processFile(File dataFile) throws Exception;

    public FileWatcher(String inputDirName, String outputDirName, String client) throws Exception {
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

                    List<String> dataFiles = processChecksumFile(inputDirName, fileName);
                    if (dataFiles != null) {
                        processFiles(dataFiles, fileName);
                        for (String dataFile : dataFiles) {
                            new File(inputDirName, dataFile).renameTo(new File(outputDirName, dataFile));
                        }
                    }
                }

            }

            boolean valid = watckKey.reset();
            if (!valid) {
                break;
            }
        }
    }

    private List<String> processChecksumFile(String inputDirName, String fileName) throws Exception {
        Path path = Paths.get(inputDirName + File.separator + fileName);
        List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
        List<String> dataFileNames = new ArrayList<String>();

        for (String line : lines) {
            String[] lineChunks = line.split("\\s+", 2);
            if (lineChunks.length != 2) {
                throw new Exception("Error processing " + fileName + ": '" + line + "' is not in valid format");
            }

            String checkSum = lineChunks[0];
            String dataFileName = lineChunks[1];

            File dataFile = new File(inputDirName + File.separator + dataFileName);
            if (!verifyChecksum(checkSum, dataFile)) {
                logger.error("Checksum check failed for " + dataFileName);
                return null;
            }
            dataFileNames.add(dataFileName);
        }

        return dataFileNames;
    }

    private boolean verifyChecksum(String checkSum, File dataFile) throws Exception {

        String actualCheckSum = MD5CheckSum.getMD5Checksum(dataFile.toString());
        return checkSum.equals(actualCheckSum);

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
