package com.nd.adhoc.push.adhoc.utils;


import org.apache.commons.net.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by Administrator on 2015/11/30.
 */
public class HMACSHAUtils {

    private static String HMAC_SHA = "HmacSHA256";

    /**
     * 生成签名数据
     *
     * @param data 待加密的数据
     * @param key  加密使用的key
     * @throws Exception
     */
    public static String hMac256(String data, String key) throws Exception {
        String sign = "";
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA);
            Mac mac = Mac.getInstance(HMAC_SHA);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            sign = new String(Base64.encodeBase64(rawHmac));
            return sign;
        } catch (Exception e) {
            e.getStackTrace();
        }
        return sign;
    }

}
