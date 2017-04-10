package com.nd.adhoc.push.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

/**
 * date: 2017/4/7 0007
 * author: cbs
 */

public class DeviceUtil {

    public static String generateDeviceId(Context context) {
        String mac = null;
        while (true) {
            try {
                WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifi.getConnectionInfo();
                mac = info.getMacAddress();
                if (mac != null && !mac.isEmpty()) {
                    mac = mac.replace(":", "");
                    break;
                }
                Thread.sleep(1000);
            } catch (Exception e) {

            }
        }

        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final String deviceId = mTelephonyMgr.getDeviceId() + mac;
        return deviceId;
    }
}
