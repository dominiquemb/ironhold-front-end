package com.reqo.ironhold.storage.security;

import junit.framework.Assert;
import org.elasticsearch.common.Base64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * User: ilya
 * Date: 3/24/13
 * Time: 8:35 AM
 */
public class LocalKeyStoreServiceTest {
    @Rule
    public TemporaryFolder parentFolder = new TemporaryFolder();

    @Test
    public void testConcurrentAccess() throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        File keyStore = new File(parentFolder.getRoot().getAbsoluteFile() + File.separator + "keystore");
        IKeyStoreService keyStoreService1 = new LocalKeyStoreService(keyStore);
        Key aKey = keyStoreService1.getKey("test", "a");
        IKeyStoreService keyStoreService2 = new LocalKeyStoreService(keyStore);
        Key bKey = keyStoreService2.getKey("test", "b");
        Key bKey2 = keyStoreService1.getKey("test", "b");


        Assert.assertEquals(Base64.encodeBytes(bKey.getEncoded()), Base64.encodeBytes(bKey2.getEncoded()));


    }
}
