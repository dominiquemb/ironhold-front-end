package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.model.exceptions.CheckSumFailedException;
import com.reqo.ironhold.storage.model.exceptions.MessageExistsException;
import com.reqo.ironhold.storage.security.AESHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.crypto.AesCipherService;
import org.elasticsearch.common.Base64;

import javax.crypto.SecretKey;
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
    private final File keyStore;
    private final Map<String, Key> keyCache;

    public LocalMimeMailMessageStorageService(File parent, File keyStore) {
        this.parent = parent;
        this.keyStore = keyStore;
        this.keyCache = new ConcurrentHashMap<>();
    }


    @Override
    public boolean exists(String client, String partition, String messageId) throws Exception {
        return getFile(client, partition, messageId).exists();
    }

    @Override
    public long store(String client, String partition, String messageId, String serializedMailMessage, String checkSum) throws Exception {
        File file = getFile(client, partition, messageId);
        File checkSumFile = getCheckSumFile(client, partition, messageId);
        if (!exists(client, partition, messageId)) {
            FileUtils.writeStringToFile(file, AESHelper.encrypt(serializedMailMessage, getKey(client, partition)));
            FileUtils.writeStringToFile(checkSumFile, checkSum);
            verifyFile(client, partition, messageId);
        } else {
            throw new MessageExistsException(client, messageId);
        }

        return file.length();
    }


    @Override
    public String get(String client, String partition, String messageId) throws Exception {
        return verifyFile(client, partition, messageId);
    }

    /**
     * Utility Methods *
     */

    private String verifyFile(String client, String partition, String messageId) throws Exception {
        File file = getFile(client, partition, messageId);
        File checkSumFile = getCheckSumFile(client, partition, messageId);

        String decrypted = AESHelper.decrypt(FileUtils.readFileToString(file), getKey(client, partition));

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

    private File getCheckSumFile(String client, String partition, String messageId) {
        return new File(parent.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + FilenameUtils.normalize(messageId) + ".checksum");
    }

    private File getFile(String client, String partition, String messageId) {
        return new File(parent.getAbsolutePath() + File.separator + client + File.separator + partition + File.separator + FilenameUtils.normalize(messageId) + ".eml");
    }

    private String getCacheKey(String client, String partition) {
        return client + "." + partition;
    }

    private Key getKey(String client, String partition) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        if (!keyCache.containsKey(getCacheKey(client, partition))) {
            updateCache();
        }

        if (!keyCache.containsKey(getCacheKey(client, partition))) {
            AesCipherService cipher = new AesCipherService();

            //generate key with default 128 bits size
            SecretKey key = (SecretKey) cipher.generateNewKey();
            addKey(client, partition, key);

            keyCache.put(getCacheKey(client, partition), key);
        }

        return keyCache.get(getCacheKey(client, partition));
    }

    private synchronized void addKey(String client, String partition, SecretKey key) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        KeyStore ks = KeyStore.getInstance("JCEKS");
        java.io.FileInputStream fis =
                new java.io.FileInputStream(keyStore);
        ks.load(fis, "password".toCharArray());

        KeyStore.SecretKeyEntry skEntry =
                new KeyStore.SecretKeyEntry(key);
        ks.setEntry(getCacheKey(client, partition), skEntry, new KeyStore.PasswordProtection("password".toCharArray()));

        // store away the keystore
        java.io.FileOutputStream fos =
                new java.io.FileOutputStream(keyStore);
        ks.store(fos, "password".toCharArray());
        fos.close();
    }

    private synchronized void updateCache() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {

        KeyStore ks = KeyStore.getInstance("JCEKS");
        if (keyStore.exists()) {
            java.io.FileInputStream fis =
                    new java.io.FileInputStream(keyStore);
            ks.load(fis, "password".toCharArray());
            fis.close();

            Enumeration<String> enumeration = ks.aliases();
            while (enumeration.hasMoreElements()) {
                String alias = enumeration.nextElement();
                if (!keyCache.containsKey(alias)) {
                    Key pk = ks.getKey(alias, "password".toCharArray());

                    keyCache.put(alias, pk);
                }
            }
        } else {
            ks.load(null, null);
            java.io.FileOutputStream fos =
                    new java.io.FileOutputStream(keyStore);
            ks.store(fos, "password".toCharArray());
        }


    }


}
