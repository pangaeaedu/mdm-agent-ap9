package com.nd.mdm.device_control;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.nd.adhoc.push.adhoc.sdk.PushSdkModule;
import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.android.adhoc.basic.log.Logger;
import com.nd.android.adhoc.basic.util.system.AdhocDeviceUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Ap9Control_hardware {
    private static final String TAG = "Ap9Control_hardware";
    static final Context context = AdhocBasicConfig.getInstance().getAppContext();

    public static void OpenWifi(Context mContext) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        mContext.startActivity(intent);
    }

    private static String getCPUSerial() {
        String cpuSerial = "";
        try {
            File file = new File("/proc/cpuinfo");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("Serial")) {
                    String[] array = line.split(":");
                    if (array.length > 1) {
                        cpuSerial = array[1].trim();
                    }
                    break;
                }
            }
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cpuSerial;
    }

    public static String makeContentHardwareInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("push_id", PushSdkModule.getInstance().getDeviceid());
            JSONObject hardwareObject = new JSONObject();
            hardwareObject.put("serial_no", AdhocDeviceUtil.getAndroidId(context));
            hardwareObject.put("cpu_sn", getCPUSerial());
            hardwareObject.put("imei", "");
            hardwareObject.put("wifi_mac", AdhocDeviceUtil.getWifiMac(context));
            hardwareObject.put("btooth_mac", AdhocDeviceUtil.getBloothMac());
            hardwareObject.put("mac", AdhocDeviceUtil.getLocalMacAddressFromIp());
            hardwareObject.put("lan_mac", AdhocDeviceUtil.getEthernetMac());
            hardwareObject.put("dev_type", "Ap9");
            hardwareObject.put("android_id", AdhocDeviceUtil.getAndroidId(context));

            jsonObject.put("hardware", hardwareObject);
            Logger.d(TAG,"Hardware push Json is: " + jsonObject);
            return jsonObject.toString();

        } catch (Exception e) {
            Logger.e(TAG, "make content json error: " + e);
        }

        return null;
    }
}
