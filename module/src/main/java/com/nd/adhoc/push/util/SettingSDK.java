package com.nd.adhoc.push.util;

import java.lang.reflect.Method;

/**
 * Created by XWQ on 2018/7/24 0024.
 */

public class SettingSDK {
    public static String getSystemVersion(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, "unknown"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }
}
