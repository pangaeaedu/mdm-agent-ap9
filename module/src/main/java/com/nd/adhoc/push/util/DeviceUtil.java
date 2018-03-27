package com.nd.adhoc.push.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.nd.adhoc.push.module.PushSdkModule;

import org.apache.log4j.Logger;

/**
 * date: 2017/4/7 0007
 * author: cbs
 */

public class DeviceUtil {
    private static Logger log = Logger.getLogger(PushSdkModule.class.getSimpleName());

    public static String getMac(Context context) {
        String mac = null;
        while (true) {
            try {
                WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifi.getConnectionInfo();
                mac = info.getMacAddress();
                if (mac != null && !mac.isEmpty()) {
                    mac = mac.replace(":", "");
                    log.warn("get mac  " + mac);
                    break;
                }
                log.warn("get mac failed, will retry after 1 second");
                Thread.sleep(1000);
            } catch (Exception e) {
                log.warn("get mac exception " + e.toString());
            }
        }
        return mac;
    }

    private static String sPseudoID = "35" + //we make this look like a valid IMEI
            Build.BOARD.length()%10 +
            Build.BRAND.length()%10 +
            Build.CPU_ABI.length()%10 +
            Build.DEVICE.length()%10 +
            Build.DISPLAY.length()%10 +
            Build.HOST.length()%10 +
            Build.ID.length()%10 +
            Build.MANUFACTURER.length()%10 +
            Build.MODEL.length()%10 +
            Build.PRODUCT.length()%10 +
            Build.TAGS.length()%10 +
            Build.TYPE.length()%10 +
            Build.USER.length()%10 ; //13 digits

    public static String getPseudoId() {
        return sPseudoID;
    }

    private static String sPseudoIDLong =  Build.BOARD +
            Build.BRAND +
            Build.CPU_ABI +
            Build.DEVICE +
            Build.DISPLAY +
            Build.HOST +
            Build.ID +
            Build.MANUFACTURER +
            Build.MODEL +
            Build.PRODUCT +
            Build.TAGS +
            Build.TYPE +
            Build.USER ; //13 digits
    public static String getPseudoIDLong() {
        return sPseudoIDLong;
    }

    public static String getAndroidId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            log.warn("get android failed , " + e.toString());
            return "";
        }
    }

    public static String getImei(Context context) {
        try {
            TelephonyManager TelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            return TelephonyMgr.getDeviceId();
        } catch (Exception e) {
            log.warn("get imei failed, " + e.toString());
            return "";
        }
    }

    public static String getManufactorer() {
        return Build.MANUFACTURER;
    }
}
