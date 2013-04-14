package com.reqo.ironhold.storage.security;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.crypto.AesCipherService;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: ilya
 * Date: 3/24/13
 * Time: 8:28 AM
 */
public class LocalKeyStoreService implements IKeyStoreService {
    public static final String KEYSTORE_EXT = ".keystore";
    private static Logger logger = Logger.getLogger(LocalKeyStoreService.class);

    private final File keyStore;
    private final Map<String, Key> keyCache;

    public LocalKeyStoreService(File keyStore) throws IOException {
        this.keyStore = keyStore;
        this.keyCache = new ConcurrentHashMap<>();

        if (!keyStore.exists()) {
            FileUtils.forceMkdir(keyStore);
        }
    }

    private String getCacheKey(String client, String partition) {
        return client + "." + partition;
    }

    public Key getKey(String client, String partition) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
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
        logger.info("Adding new key for " + getCacheKey(client, partition));
        KeyStore ks = KeyStore.getInstance("JCEKS");
        if (getKeyStoreForClient(client).exists()) {
            java.io.FileInputStream fis =
                    new java.io.FileInputStream(getKeyStoreForClient(client));
            ks.load(fis, getPassword(client));
        } else {
            ks.load(null, null);
            FileOutputStream fos = new FileOutputStream(getKeyStoreForClient(client));
            ks.store(fos, getPassword(client));
        }
        KeyStore.SecretKeyEntry skEntry =
                new KeyStore.SecretKeyEntry(key);
        ks.setEntry(getCacheKey(client, partition), skEntry, new KeyStore.PasswordProtection(getPassword(client)));

        // store away the keystore
        java.io.FileOutputStream fos =
                new java.io.FileOutputStream(getKeyStoreForClient(client));
        ks.store(fos, getPassword(client));
        fos.close();
    }

    private synchronized void updateCache() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (!keyStore.exists()) {
            FileUtils.forceMkdir(keyStore);
        }
        KeyStore ks = KeyStore.getInstance("JCEKS");
        for (File keyStoreFile : keyStore.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(KEYSTORE_EXT);
            }
        })) {
            java.io.FileInputStream fis =
                    new java.io.FileInputStream(keyStoreFile);
            ks.load(fis, getPassword(keyStoreFile.getName().replace(KEYSTORE_EXT, "")));
            fis.close();

            Enumeration<String> enumeration = ks.aliases();
            while (enumeration.hasMoreElements()) {
                String alias = enumeration.nextElement();
                if (!keyCache.containsKey(alias)) {
                    Key pk = ks.getKey(alias, getPassword(keyStoreFile.getName().replace(KEYSTORE_EXT, "")));

                    keyCache.put(alias, pk);
                }
            }
        }


    }

    public File getKeyStore() {
        return keyStore;
    }

    public File getKeyStoreForClient(String client) {
        return new File(keyStore + File.separator + client + KEYSTORE_EXT);
    }

    public char[] getPassword(String client) {
        return "password".toCharArray();
    }

}
