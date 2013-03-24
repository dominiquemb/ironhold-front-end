package com.reqo.ironhold.storage.security;

import org.apache.log4j.Logger;
import org.apache.shiro.crypto.AesCipherService;

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
 * Date: 3/24/13
 * Time: 8:28 AM
 */
public class LocalKeyStoreService implements IKeyStoreService {
    private static Logger logger = Logger.getLogger(LocalKeyStoreService.class);

    private final File keyStore;
    private final Map<String, Key> keyCache;

    public LocalKeyStoreService(File keyStore) {
        this.keyStore = keyStore;
        this.keyCache = new ConcurrentHashMap<>();
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
