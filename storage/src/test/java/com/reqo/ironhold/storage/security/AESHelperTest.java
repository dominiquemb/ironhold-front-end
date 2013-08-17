package com.reqo.ironhold.storage.security;

import org.apache.shiro.crypto.AesCipherService;
import org.fluttercode.datafactory.impl.DataFactory;
import org.junit.Assert;
import org.junit.Test;

import java.security.Key;


/**
 * User: ilya
 * Date: 3/23/13
 * Time: 2:10 PM
 */
public class AESHelperTest {

    @Test
    public void testAESEncryptionDecryption() throws Exception {
        DataFactory df = new DataFactory();
        String testString = df.getRandomText(4000000);

        AesCipherService cipher = new AesCipherService();

        //generate key with default 128 bits size
        Key key = cipher.generateNewKey();


        String encrypted = AESHelper.encrypt(testString, key);
        System.out.println(encrypted.length());
        String decrypted = AESHelper.decrypt(encrypted, key);
        System.out.println(decrypted.length());

        Assert.assertEquals(testString, decrypted);
    }


}
