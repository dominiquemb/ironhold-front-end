package com.reqo.ironhold.storage.security;

import org.elasticsearch.common.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User: ilya
 * Date: 3/24/13
 * Time: 10:21 AM
 */
public class CheckSumHelper {
    public static String getCheckSum(byte[] content) throws NoSuchAlgorithmException {
        MessageDigest complete = MessageDigest.getInstance("MD5");
        complete.update(content);

        return Base64.encodeBytes(complete.digest());
    }
}
