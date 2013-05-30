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


    private final File parent;
    private final IKeyStoreService keyStoreService;

    public LocalMimeMailMessageStorageService(File parent, IKeyStoreService keyStoreService) throws IOException {
        this.parent = parent;
        this.keyStoreService = keyStoreService;

        if (!parent.exists()) {
            FileUtils.forceMkdir(parent);
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
        File parentDir = new File(parent.getAbsoluteFile() + File.separator + clientName);

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
        File parentDir = new File(parent.getAbsoluteFile() + File.separator + clientName + File.separator + partition);
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
        File parentDir = new File(parent.getAbsoluteFile() + File.separator + clientName + File.separator + partition + File.separator + subPartition);
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

    private File getCheckSumFile(String client, String partition, String subPartition, String messageId) {
        return new File(parent.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + subPartition + File.separator + FilenameUtils.normalize(messageId) + ".checksum");
    }

    private File getFile(String client, String partition, String subPartition, String messageId) {
        return new File(parent.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + subPartition + File.separator + FilenameUtils.normalize(messageId) + ".eml.gz");
    }


    public File getParent() {
        return parent;
    }
}
