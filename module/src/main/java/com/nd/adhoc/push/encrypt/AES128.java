package com.nd.adhoc.push.encrypt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

/*
 * AES 加密算法
 */
public class AES128 implements SymmetricCipher {
    private Cipher cipherEnc;
    private Cipher cipherDec;

    private SecretKeySpec getKeySpec(String key) {
        StringBuffer pad = new StringBuffer();
        pad.append(key);
        if (key.length()<16){
            for (int i=0; i<16-key.length(); ++i){
                pad.append('\0');
            }
        }
        byte[] keyBytes = pad.toString().getBytes();
        return new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public void init(String key) throws Exception {
        SecretKeySpec spec = getKeySpec(key);
        cipherEnc = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
        cipherEnc.init(Cipher.ENCRYPT_MODE, spec);// 初始化
        cipherDec = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
        cipherDec.init(Cipher.DECRYPT_MODE, spec);// 初始化
    }

    @Override
    public byte[] encrypt(byte[] data) throws BadPaddingException, IllegalBlockSizeException {
        return aesEncrypt(data);
    }

    @Override
    public byte[] decrypt(byte[] data, int position, int length) throws BadPaddingException, IllegalBlockSizeException {
        return aesDecrypt(data, position, length);
    }


    public byte[] aesEncrypt(byte []content) throws BadPaddingException, IllegalBlockSizeException {
        byte[] result = cipherEnc.doFinal(content);
        return result;
    }

    public byte[] aesDecrypt(byte []content, int position, int length) throws BadPaddingException, IllegalBlockSizeException {
        byte[] result = cipherDec.doFinal(content, position, length);
        return result;
    }
}