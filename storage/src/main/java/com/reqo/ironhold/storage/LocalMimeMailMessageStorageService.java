package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.model.exceptions.CheckSumFailedException;
import com.reqo.ironhold.storage.model.exceptions.MessageExistsException;
import com.reqo.ironhold.storage.security.RSAHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.Base64;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: ilya
 * Date: 3/19/13
 * Time: 8:25 PM
 */
public class LocalMimeMailMessageStorageService implements IMimeMailMessageStorageService {
    private static Logger logger = Logger.getLogger(LocalMimeMailMessageStorageService.class);

    private final File parent;
    private final File privateKeyStore;
    private final Map<String, PrivateKey> privateKeyCache;

    public LocalMimeMailMessageStorageService(File parent, File privateKeyStore) {
        this.parent = parent;
        this.privateKeyStore = privateKeyStore;
        this.privateKeyCache = new ConcurrentHashMap<>();
    }


    @Override
    public boolean exists(String client, String messageId) throws Exception {
        return getFile(client, messageId).exists();
    }

    @Override
    public long store(String client, String messageId, String serializedMailMessage, String checkSum) throws Exception {
        File file = getFile(client, messageId);
        File checkSumFile = getCheckSumFile(client, messageId);
        if (!exists(client, messageId)) {
            FileUtils.writeStringToFile(file, serializedMailMessage);
            FileUtils.writeStringToFile(checkSumFile, checkSum);
            verifyFile(client, messageId);
        } else {
            throw new MessageExistsException(client, messageId);
        }

        return file.length();
    }


    @Override
    public String get(String client, String messageId) throws Exception {
        return verifyFile(client, messageId);
    }

    /**
     * Utility Methods *
     */

    private String verifyFile(String client, String messageId) throws Exception {
        File file = getFile(client, messageId);
        File checkSumFile = getCheckSumFile(client, messageId);

        String decrypted = RSAHelper.decrypt(FileUtils.readFileToString(file), getPrivateKey(client));

        byte[] decryptedBytes = decrypted.getBytes();

        MessageDigest complete = MessageDigest.getInstance("MD5");
        complete.update(decryptedBytes);

        String actualChecksum = Base64.encodeBytes(complete.digest());
        String expectedChecksum = FileUtils.readFileToString(checkSumFile);
        if (!actualChecksum.equals(expectedChecksum)) {
            throw new CheckSumFailedException(file);
        }

        return decrypted;
    }

    private File getCheckSumFile(String client, String messageId) {
        return new File(parent.getAbsolutePath() + File.separator + client + File.separator + FilenameUtils.normalize(messageId) + ".checksum");
    }

    private File getFile(String client, String messageId) {
        return new File(parent.getAbsolutePath() + File.separator + client + File.separator + FilenameUtils.normalize(messageId) + ".eml");
    }

    private PrivateKey getPrivateKey(String client) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        if (!privateKeyCache.containsKey(client)) {
            updateCache();
        }

        return privateKeyCache.get(client);
    }

    private void updateCache() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        java.io.FileInputStream fis =
                new java.io.FileInputStream(privateKeyStore);
        ks.load(fis, "password".toCharArray());
        fis.close();

        Enumeration<String> enumeration = ks.aliases();
        while (enumeration.hasMoreElements()) {
            String alias = enumeration.nextElement();
            if (!privateKeyCache.containsKey(alias)) {
                PrivateKey pk = (PrivateKey) ks.getKey(alias, "password".toCharArray());

                privateKeyCache.put(alias, pk);
            }
        }
    }


}
