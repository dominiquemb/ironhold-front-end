package com.reqo.ironhold.importer.watcher;

import com.reqo.ironhold.importer.notification.EmailNotification;
import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public abstract class FileWatcher {
    static {
        System.setProperty("jobname", FileWatcher.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(FileWatcher.class);

    private String inputDirName;
    private String outputDirName;
    private String quarantineDirName;
    private String client;

    private WatchService watchService;

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


    public void start() throws IOException {
        logger.info("Watching " + inputDirName + " directory for " + client);
        Path inputDir = Paths.get(inputDirName);
        watchService = inputDir.getFileSystem().newWatchService();
        inputDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        while (true) {
            WatchKey watckKey;
            try {
                watckKey = watchService.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
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
                    logger.info("Processing md5 file: " + fileName);

                    try {
                        Thread.sleep(100);
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
                        EmailNotification.sendSystemNotification("Failed to process " + fileName,
                                e.getMessage());
                        logger.warn("Failed to process " + fileName, e);
                    }

                }

            }

            boolean valid = watckKey.reset();
            if (!valid) {
                break;
            }
        }
    }

    public void deActivate() {
        try {
            watchService.close();
        } catch (IOException e) {
            logger.warn(e);
        }
    }


    public boolean isStarted() {
        return watchService != null;
    }

    private MD5CheckSum processChecksumFile(String inputDirName,
                                            String checkSumfileName) throws Exception {

        MD5CheckSum checkSum = new MD5CheckSum(new File(getInputDirName(),
                checkSumfileName));

        if (!checkSum.verifyChecksum()) {
            logger.warn("Checksum check failed for " + checkSumfileName);
            quarantine(checkSum, "Checksum check failed for "
                    + checkSumfileName);
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

        EmailNotification.sendSystemNotification("Quarantining file "
                + checkSum.getCheckSumFile().toString(), reason);
    }

    public String getInputDirName() {
        return inputDirName;
    }

    public String getOutputDirName() {
        return outputDirName;
    }

    public String getQuarantineDirName() {
        return quarantineDirName;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public void setInputDirName(String inputDirName) {
        this.inputDirName = inputDirName;
    }

    public void setOutputDirName(String outputDirName) {
        this.outputDirName = outputDirName;
    }

    public void setQuarantineDirName(String quarantineDirName) {
        this.quarantineDirName = quarantineDirName;
    }

    public String getClient() {
        return client;
    }
}
