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
        return getFile(client, partition, subPartition, messageId).exists();
    }

    @Override
    public long store(String client, String partition, String subPartition, String messageId, String serializedMailMessage, String checkSum) throws Exception {
        File file = getFile(client, partition, subPartition, messageId);
        File checkSumFile = getCheckSumFile(client, partition, subPartition, messageId);
        if (!exists(client, partition, subPartition, messageId)) {
            FileUtils.writeStringToFile(file, AESHelper.encrypt(Compression.compress(serializedMailMessage), keyStoreService.getKey(client, partition)));
            FileUtils.writeStringToFile(checkSumFile, checkSum);
            verifyFile(client, partition, subPartition, messageId);
        } else {
            throw new MessageExistsException(client, messageId);
        }

        return file.length();
    }


    @Override
    public String get(String client, String partition, String subPartition, String messageId) throws Exception {
        return verifyFile(client, partition, subPartition, messageId);
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
                return (name.endsWith(".eml.gz"));
            }
        });
        List<String> files = new ArrayList();
        for (File f : result) {
            if (!f.isDirectory()) {
                files.add(f.getName().replace(".eml.gz", ""));
            }
        }
        return files;
    }

    @Override
    public boolean archive(String clientName, String partition, String subPartition, String messageId) throws Exception {
        verifyFile(clientName, partition, subPartition, messageId);

        File file = getFile(clientName, partition, subPartition, messageId);
        File checkSumFile = getCheckSumFile(clientName, partition, subPartition, messageId);

        if (!file.exists()) {
            return false;
        }

        if (!checkSumFile.exists()) {
            return false;
        }

        FileUtils.copyFile(file, getArchiveFile(clientName, partition, subPartition, messageId));

        FileUtils.copyFile(checkSumFile, getArchiveCheckSumFile(clientName, partition, subPartition, messageId));

        verifyArchiveFile(clientName, partition, subPartition, messageId);

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
        File file = getFile(client, partition, subPartition, messageId);
        File checkSumFile = getCheckSumFile(client, partition, subPartition, messageId);

        String decrypted = Compression.decompress(AESHelper.decrypt(FileUtils.readFileToString(file), keyStoreService.getKey(client, partition)));
        byte[] decryptedBytes = decrypted.getBytes();

        String actualChecksum = CheckSumHelper.getCheckSum(decryptedBytes);
        String expectedChecksum = FileUtils.readFileToString(checkSumFile);
        if (!actualChecksum.equals(expectedChecksum)) {
            throw new CheckSumFailedException(file);
        }

        return decrypted;
    }

    private String verifyArchiveFile(String client, String partition, String subPartition, String messageId) throws Exception {
        File file = getArchiveFile(client, partition, subPartition, messageId);
        File checkSumFile = getArchiveCheckSumFile(client, partition, subPartition, messageId);

        String decrypted = Compression.decompress(AESHelper.decrypt(FileUtils.readFileToString(file), keyStoreService.getKey(client, partition)));
        byte[] decryptedBytes = decrypted.getBytes();

        String actualChecksum = CheckSumHelper.getCheckSum(decryptedBytes);
        String expectedChecksum = FileUtils.readFileToString(checkSumFile);
        if (!actualChecksum.equals(expectedChecksum)) {
            throw new CheckSumFailedException(file);
        }

        return decrypted;
    }

    private File getCheckSumFile(String client, String partition, String subPartition, String messageId) {
        return new File(dataStore.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + subPartition + File.separator + FilenameUtils.normalize(messageId) + ".checksum");
    }

    private File getFile(String client, String partition, String subPartition, String messageId) {
        return new File(dataStore.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + subPartition + File.separator + FilenameUtils.normalize(messageId) + ".eml.gz");
    }

    private File getArchiveCheckSumFile(String client, String partition, String subPartition, String messageId) {
        return new File(archiveStore.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + subPartition + File.separator + FilenameUtils.normalize(messageId) + ".checksum");
    }

    private File getArchiveFile(String client, String partition, String subPartition, String messageId) {
        return new File(archiveStore.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + subPartition + File.separator + FilenameUtils.normalize(messageId) + ".eml.gz");
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
}
