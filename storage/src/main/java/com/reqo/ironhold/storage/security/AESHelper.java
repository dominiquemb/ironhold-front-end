package com.reqo.ironhold.storage.security;

import org.apache.log4j.Logger;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;
import org.elasticsearch.common.Base64;

import java.io.IOException;
import java.security.Key;

/**
 * User: ilya
 * Date: 3/23/13
 * Time: 9:27 PM
 */
public class AESHelper {
    private static Logger logger = Logger.getLogger(AESHelper.class);

    public static String encrypt(String plainText, Key key) {
        long started = System.currentTimeMillis();
        try {
            AesCipherService cipher = new AesCipherService();

            byte[] keyBytes = key.getEncoded();

            //encrypt the secret
            byte[] secretBytes = CodecSupport.toBytes(plainText);
            ByteSource encrypted = cipher.encrypt(secretBytes, keyBytes);
            return Base64.encodeBytes(encrypted.getBytes());
        } finally {
            long finished = System.currentTimeMillis();
            logger.info("Encrypted " + plainText.length() + " bytes in " + (finished - started) + "ms");
        }
    }

    public static String decrypt(String encryptedText, Key key) throws IOException {
        long started = System.currentTimeMillis();
        try {
            AesCipherService cipher = new AesCipherService();

            byte[] encryptedBytes = Base64.decode(encryptedText);

            ByteSource decrypted = cipher.decrypt(encryptedBytes, key.getEncoded());
            return new String(decrypted.getBytes());
        } finally {
            long finished = System.currentTimeMillis();
            logger.info("Decrypted " + encryptedText.length() + " bytes in " + (finished - started) + "ms");
        }
    }


}
