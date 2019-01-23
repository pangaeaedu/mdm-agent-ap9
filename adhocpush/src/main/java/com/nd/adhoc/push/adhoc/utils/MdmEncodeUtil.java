package com.nd.adhoc.push.adhoc.utils;

/**
 * Created by HuangYK on 2018/8/7.
 */

public class MdmEncodeUtil {


    public static String encode(String content) {
        try {
            return HMACSHAUtils.hMac256(content, "101DRMS");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
