package com.reqo.ironhold.storage.security;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sun.security.x509.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * /**
 * User: ilya
 * Date: 3/23/13
 * Time: 9:02 AM
 */
public class SecurityUtilities {
    private static Logger logger = Logger.getLogger(SecurityUtilities.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /* Create a self-signed X.509 Certificate
    * @param dn the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
    * @param pair the KeyPair
    * @param days how many days from now the Certificate is valid for
    * @param algorithm the signing algorithm, eg "SHA1withRSA"
    */
    public static X509Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm)
            throws GeneralSecurityException, IOException {
        PrivateKey privkey = pair.getPrivate();
        X509CertInfo info = new X509CertInfo();
        Date from = new Date();
        Date to = new Date(from.getTime() + days * 86400000l);
        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger sn = new BigInteger(64, new SecureRandom());
        X500Name owner = new X500Name(dn);

        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
        info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
        info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
        info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(privkey, algorithm);

        // Update the algorith, and resign.
        algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(privkey, algorithm);
        return cert;
    }

    public static String encrypt(String plaintext, PublicKey key) throws Exception {
        long started = System.currentTimeMillis();
        try {
            Cipher cipher = Cipher.getInstance("RSA/NONE/NoPadding");

            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] bytes = plaintext.getBytes("UTF-8");

            byte[] encrypted = blockCipher(cipher, bytes, Cipher.ENCRYPT_MODE);

            char[] encryptedTransferable = Hex.encodeHex(encrypted);
            return new String(encryptedTransferable);
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

            byte[] bts = Hex.decodeHex(encrypted.toCharArray());

            byte[] decrypted = blockCipher(cipher, bts, Cipher.DECRYPT_MODE);


            return new String(decrypted, "UTF-8");
        } finally {
            long finished = System.currentTimeMillis();
            logger.info("Decrypted " + encrypted.length() + " bytes in " + (finished - started) + "ms");
        }

    }

    public static byte[] blockCipher(Cipher cipher, byte[] bytes, int mode) throws IllegalBlockSizeException, BadPaddingException {
        // string initialize 2 buffers.
        // scrambled will hold intermediate results
        byte[] scrambled = new byte[0];

        // toReturn will hold the total result
        byte[] toReturn = new byte[0];
        // if we encrypt we use 100 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
        int length = (mode == Cipher.ENCRYPT_MODE) ? 100 : 128;

        // another buffer. this one will hold the bytes that have to be modified in this step
        byte[] buffer = new byte[length];

        for (int i = 0; i < bytes.length; i++) {

            // if we filled our buffer array we have our block ready for de- or encryption
            if ((i > 0) && (i % length == 0)) {
                //execute the operation
                scrambled = cipher.doFinal(buffer);
                // add the result to our total result.
                toReturn = append(toReturn, scrambled);
                // here we calculate the length of the next buffer required
                int newlength = length;

                // if newlength would be longer than remaining bytes in the bytes array we shorten it.
                if (i + length > bytes.length) {
                    newlength = bytes.length - i;
                }
                // clean the buffer array
                buffer = new byte[newlength];
            }
            // copy byte into our buffer.
            buffer[i % length] = bytes[i];
        }

        // this step is needed if we had a trailing buffer. should only happen when encrypting.
        // example: we encrypt 110 bytes. 100 bytes per run means we "forgot" the last 10 bytes. they are in the buffer array
        scrambled = cipher.doFinal(buffer);

        // final step before we can return the modified data.
        toReturn = append(toReturn, scrambled);

        return toReturn;
    }

    private static byte[] append(byte[] prefix, byte[] suffix) {
        byte[] toReturn = new byte[prefix.length + suffix.length];
        for (int i = 0; i < prefix.length; i++) {
            toReturn[i] = prefix[i];
        }
        for (int i = 0; i < suffix.length; i++) {
            toReturn[i + prefix.length] = suffix[i];
        }
        return toReturn;
    }
}
