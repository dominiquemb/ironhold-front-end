package com.reqo.ironhold.storage.security;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * User: ilya
 * Date: 3/24/13
 * Time: 8:26 AM
 */
public interface IKeyStoreService {
    Key getKey(String client, String partition) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException;
}
