package com.reqo.ironhold.storage.security;

import com.reqo.ironhold.storage.utils.ByteArrayUtilities;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.elasticsearch.common.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

/**
 * User: ilya
 * Date: 3/23/13
 * Time: 2:55 PM
 */
public class RSAHelper {
    private static Logger logger = Logger.getLogger(SecurityUtilities.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static String encrypt(String plaintext, PublicKey key) throws Exception {
        long started = System.currentTimeMillis();
        try {
            Cipher cipher = Cipher.getInstance("RSA/NONE/NoPadding");

            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] bytes = plaintext.getBytes("UTF-8");

            return Base64.encodeBytes(blockCipher(cipher, bytes, Cipher.ENCRYPT_MODE));
        } finally {
            long finished = System.currentTimeMillis();
            logger.info("Encrypted " + plaintext.length() + " bytes in " + (finished - started) + "ms");
        }
    }

    public static String decrypt(String encrypted, PrivateKey key) throws Exception {
        long started = System.currentTimeMillis();
        try {
            Cipher cipher = Cipher.getInstance("RSA/NONE/NoPadding");

            cipher.init(Cipher.DECRYPT_MODE, key);

            return new String(blockCipher(cipher, Base64.decode(encrypted), Cipher.DECRYPT_MODE), "UTF-8");
        } finally {
            long finished = System.currentTimeMillis();
            logger.info("Decrypted " + encrypted.length() + " bytes in " + (finished - started) + "ms");
        }

    }

    public static byte[] blockCipher(Cipher cipher, byte[] bytes, int mode) throws IllegalBlockSizeException, BadPaddingException, IOException {
        // string initialize 2 buffers.
        // scrambled will hold intermediate results
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);

        // if we encrypt we use 100 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
        int length = (mode == Cipher.ENCRYPT_MODE) ? 100 : 128;


        byte[][] chunks = ByteArrayUtilities.divideArray(bytes, length);
        for (byte[] chunk : chunks) {
            bos.write(cipher.doFinal(chunk));

        }

        bos.close();
        return bos.toByteArray();
    }
}
