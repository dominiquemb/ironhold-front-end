package com.reqo.ironhold.storage.security;

import junit.framework.Assert;
import org.apache.shiro.crypto.AesCipherService;
import org.fluttercode.datafactory.impl.DataFactory;
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
        String testString = df.getRandomText(400000);

        AesCipherService cipher = new AesCipherService();

        //generate key with default 128 bits size
        Key key = cipher.generateNewKey();


        String encrypted = AESHelper.encrypt(testString, key);

        String decrypted = AESHelper.decrypt(encrypted, key);

        Assert.assertEquals(testString, decrypted);
    }


}
