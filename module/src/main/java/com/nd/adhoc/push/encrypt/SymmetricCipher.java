package com.nd.adhoc.push.encrypt;

/**
 *
 */
public interface SymmetricCipher {
    void init(String key) throws Exception;
    byte[] encrypt(byte[] data) throws Exception;
    byte[] decrypt(byte[] data, int position, int length) throws Exception;
}
