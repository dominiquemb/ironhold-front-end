package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.model.exceptions.CheckSumFailedException;
import com.reqo.ironhold.storage.model.exceptions.MessageExistsException;
import com.reqo.ironhold.storage.security.AESHelper;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import com.reqo.ironhold.storage.security.IKeyStoreService;
import com.reqo.ironhold.storage.utils.Compression;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: ilya
 * Date: 3/19/13
 * Time: 8:25 PM
 */
public class LocalMimeMailMessageStorageService implements IMimeMailMessageStorageService {
    private static Logger logger = Logger.getLogger(LocalMimeMailMessageStorageService.class);


    private final File dataStore;
    private final File archiveStore;
    private final IKeyStoreService keyStoreService;

    public LocalMimeMailMessageStorageService(File dataStore, File archiveStore, IKeyStoreService keyStoreService) throws IOException {
        this.dataStore = dataStore;
        this.archiveStore = archiveStore;
        this.keyStoreService = keyStoreService;

        if (!dataStore.exists()) {
            FileUtils.forceMkdir(dataStore);
        }


        if (!archiveStore.exists()) {
            FileUtils.forceMkdir(archiveStore);
        }
    }


    @Override
    public boolean exists(String client, String partition, String subPartition, String messageId) throws Exception {
        return getExistingFile(client, partition, subPartition, normalizeMessageId(messageId)) != null;
    }

    @Override
    public boolean isEncrypted(String client, String partition, String subPartition, String messageId) throws Exception {
        File file = getExistingFile(client, partition, subPartition, normalizeMessageId(messageId));
        return isEncrypted(file);
    }

    @Override
    public long store(String client, String partition, String subPartition, String messageId, String serializedMailMessage, String checkSum, boolean encrypt) throws Exception {
        File file = getNewFile(client, partition, subPartition, normalizeMessageId(messageId), encrypt);
        File checkSumFile = getCheckSumFile(client, partition, subPartition, normalizeMessageId(messageId));
        if (!exists(client, partition, subPartition, normalizeMessageId(messageId))) {
            if (encrypt) {
                FileUtils.writeStringToFile(file, AESHelper.encrypt(Compression.compress(serializedMailMessage), keyStoreService.getKey(client, partition)));
            } else {
                FileUtils.writeStringToFile(file, Compression.compress(serializedMailMessage));
            }
            FileUtils.writeStringToFile(checkSumFile, checkSum);
            verifyFile(client, partition, subPartition, normalizeMessageId(messageId));
        } else {
            throw new MessageExistsException(client, normalizeMessageId(messageId));
        }

        return file.length();
    }


    @Override
    public String get(String client, String partition, String subPartition, String messageId) throws Exception {
        return verifyFile(client, partition, subPartition, normalizeMessageId(messageId));
    }

    @Override
    public List<String> getPartitions(String clientName) {
        File parentDir = new File(dataStore.getAbsoluteFile() + File.separator + clientName);

        File[] result = parentDir.listFiles();
        List<String> partitions = new ArrayList();
        for (File f : result) {
            if (f.isDirectory()) {
                partitions.add(f.getName());
            }
        }
        return partitions;
    }

    @Override
    public List<String> getSubPartitions(String clientName, String partition) {
        File parentDir = new File(dataStore.getAbsoluteFile() + File.separator + clientName + File.separator + partition);
        File[] result = parentDir.listFiles();
        List<String> subPartitions = new ArrayList();
        for (File f : result) {
            if (f.isDirectory()) {
                subPartitions.add(f.getName());
            }
        }
        return subPartitions;
    }

    @Override
    public List<String> getList(String clientName, String partition, String subPartition) {
        File parentDir = new File(dataStore.getAbsoluteFile() + File.separator + clientName + File.separator + partition + File.separator + subPartition);
        File[] result = parentDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith(".eml.gz") || name.endsWith("eml.gz.aes"));
            }
        });
        List<String> files = new ArrayList();
        for (File f : result) {
            if (!f.isDirectory()) {
                if (f.getName().endsWith(".eml.gz")) {
                    files.add(f.getName().replace(".eml.gz", ""));
                }
                if (f.getName().endsWith(".eml.gz.aes")) {
                    files.add(f.getName().replace(".eml.gz.aes", ""));
                }

            }
        }
        return files;
    }

    @Override
    public boolean archive(String clientName, String partition, String subPartition, String messageId) throws Exception {
        verifyFile(clientName, partition, subPartition, normalizeMessageId(messageId));

        File file = getExistingFile(clientName, partition, subPartition, normalizeMessageId(messageId));
        File checkSumFile = getCheckSumFile(clientName, partition, subPartition, normalizeMessageId(messageId));

        if (!file.exists()) {
            return false;
        }

        if (!checkSumFile.exists()) {
            return false;
        }

        FileUtils.copyFile(file, getArchiveFile(clientName, partition, subPartition, normalizeMessageId(messageId)));

        FileUtils.copyFile(checkSumFile, getArchiveCheckSumFile(clientName, partition, subPartition, normalizeMessageId(messageId)));

        verifyArchiveFile(clientName, partition, subPartition, normalizeMessageId(messageId));

        FileUtils.forceDelete(file);
        FileUtils.forceDelete(checkSumFile);

        return true;
    }

    public List<String> getClients() {
        File parentDir = new File(dataStore.getAbsoluteFile() + File.separator);

        File[] result = parentDir.listFiles();
        List<String> clients = new ArrayList();
        for (File f : result) {
            if (f.isDirectory()) {
                clients.add(f.getName());
            }
        }
        return clients;
    }

    /**
     * Utility Methods *
     */

    private String verifyFile(String client, String partition, String subPartition, String messageId) throws Exception {
        File file = getExistingFile(client, partition, subPartition, messageId);
        File checkSumFile = getCheckSumFile(client, partition, subPartition, messageId);

        String data = null;
        if (isEncrypted(file)) {
            data = Compression.decompress(AESHelper.decrypt(FileUtils.readFileToString(file), keyStoreService.getKey(client, partition)));
        } else {
            data = Compression.decompress(FileUtils.readFileToString(file));
        }

        byte[] bytes = data.getBytes();

        String actualChecksum = CheckSumHelper.getCheckSum(bytes);
        String expectedChecksum = FileUtils.readFileToString(checkSumFile);
        if (!actualChecksum.equals(expectedChecksum)) {
            throw new CheckSumFailedException(file);
        }

        return data;
    }

    private boolean isEncrypted(File file) {
        return file.getName().endsWith(".gz.aes");
    }

    private String verifyArchiveFile(String client, String partition, String subPartition, String messageId) throws Exception {
        File file = getArchiveFile(client, partition, subPartition, messageId);
        File checkSumFile = getArchiveCheckSumFile(client, partition, subPartition, messageId);

        String data = null;
        if (isEncrypted(file)) {
            data = Compression.decompress(AESHelper.decrypt(FileUtils.readFileToString(file), keyStoreService.getKey(client, partition)));
        } else {
            data = Compression.decompress(FileUtils.readFileToString(file));
        }

        byte[] bytes = data.getBytes();

        String actualChecksum = CheckSumHelper.getCheckSum(bytes);
        String expectedChecksum = FileUtils.readFileToString(checkSumFile);
        if (!actualChecksum.equals(expectedChecksum)) {
            throw new CheckSumFailedException(file);
        }

        return data;
    }

    private File getCheckSumFile(String client, String partition, String subPartition, String messageId) {
        return new File(dataStore.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + subPartition + File.separator + FilenameUtils.normalize(messageId) + ".checksum");
    }

    private File getExistingFile(String client, String partition, String subPartition, String messageId) {
        String prefix = dataStore.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + subPartition + File.separator + FilenameUtils.normalize(messageId);
        File f = new File(prefix + ".eml.gz");
        if (f.exists()) {
            return f;
        }
        f = new File(prefix + ".eml.gz.aes");
        if (f.exists()) {
            return f;
        }
        return null;

    }

    private File getNewFile(String client, String partition, String subPartition, String messageId, boolean encrypt) throws FileNotFoundException {

        String prefix = dataStore.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + subPartition + File.separator + FilenameUtils.separatorsToUnix(FilenameUtils.normalize(messageId));
        if (!encrypt) {
            return new File(prefix + ".eml.gz");
        } else {
            return new File(prefix + ".eml.gz.aes");
        }
    }

    private File getArchiveCheckSumFile(String client, String partition, String subPartition, String messageId) {
        return new File(archiveStore.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + subPartition + File.separator + FilenameUtils.normalize(messageId) + ".checksum");
    }

    private File getArchiveFile(String client, String partition, String subPartition, String messageId) {
        File existingFile = getExistingFile(client, partition, subPartition, messageId);
        String prefix = archiveStore.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + subPartition + File.separator + FilenameUtils.normalize(messageId);
        if (existingFile.getName().endsWith(".eml.gz")) {
            return new File(prefix + ".eml.gz");
        } else {
            return new File(prefix + ".eml.gz.aes");
        }
    }

    public File getDataStore() {
        return dataStore;
    }

    public File getArchiveStore() {
        return archiveStore;
    }

    public IKeyStoreService getKeyStoreService() {
        return keyStoreService;
    }

    public static String normalizeMessageId(String messageId) {
        return messageId.replaceAll(File.separator, "");
    }
}
