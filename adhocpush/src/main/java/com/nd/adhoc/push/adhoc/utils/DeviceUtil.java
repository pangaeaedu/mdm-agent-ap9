package com.nd.adhoc.push.adhoc.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.nd.adhoc.push.adhoc.sdk.PushSdkModule;

import org.apache.log4j.Logger;

import java.util.UUID;

/**
 * date: 2017/4/7 0007
 * author: cbs
 */

public class DeviceUtil {
    private static Logger log = Logger.getLogger(PushSdkModule.class.getSimpleName());

    public static String getMac(@NonNull Context context) {
        return PushStoredDeviceInfoUtils.getDeviceMac(context);
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

    /**
     * 获得设备唯一id，该方法不如getDeviceUUID精确，已经废弃
     *
     * @param context
     * @return
     */
    @Deprecated
    public static String getUniqueId(Context context) {
        String androidID, serial;
        if (context != null) {
            androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            androidID = "unknown";
        }
        try {
            serial = Build.SERIAL;
        } catch (Exception exception) {
            serial = "serial";
        }
        return androidID + serial;
    }

    /**
     * 最终方案，获取设备ID
     *
     * @param context
     * @param allowFirmwareUpdate
     * @return
     */
    public static String getDeviceUUID(Context context, boolean allowFirmwareUpdate) {
        String uniqueID = getUniqueId(context);
        StringBuffer mostSigBitSB = new StringBuffer();
        mostSigBitSB.append(uniqueID).append("/");
        mostSigBitSB.append(Build.BRAND).append("/");
        mostSigBitSB.append(Build.MODEL).append("/");
        mostSigBitSB.append(Build.PRODUCT).append("/");
        mostSigBitSB.append(Build.DEVICE).append("/");
        mostSigBitSB.append(Build.MANUFACTURER).append("/");
        mostSigBitSB.append(Build.ID);
        if (allowFirmwareUpdate) {
            mostSigBitSB.append("/").append(Build.VERSION.INCREMENTAL);
        }
        return new UUID(mostSigBitSB.toString().hashCode(), getUniqueId(context).hashCode()).toString();
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

    // 只有 ND3 上才有
    public static String getND3RomVersion(String pDefault) {
        String systemVersion = null;
        String model = Build.MODEL;
        if (model.toLowerCase().contains("nd3")) {
            systemVersion = SettingSDK.getSystemVersion("ro.product.firmware.version", pDefault);
        }
        return systemVersion;
    }

    public static boolean isND3Device() {
        return !TextUtils.isEmpty(getND3RomVersion(""));
    }
}
