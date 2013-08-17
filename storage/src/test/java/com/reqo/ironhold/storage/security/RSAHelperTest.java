package com.reqo.ironhold.storage.security;

import org.fluttercode.datafactory.impl.DataFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;


/**
 * User: ilya
 * Date: 3/23/13
 * Time: 2:10 PM
 */
public class RSAHelperTest {
    @Test
    public void testRSAEncryptionDecryption() throws Exception {
        DataFactory df = new DataFactory();
        String testString = df.getRandomText(4000);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair keypair = kpg.generateKeyPair();
        Cipher cipher = Cipher.getInstance("RSA");

        String encrypted = RSAHelper.encrypt(testString, keypair.getPublic());

        String decrypted = RSAHelper.decrypt(encrypted, keypair.getPrivate());

        Assert.assertEquals(testString, decrypted);
    }


}
