package com.nd.adhoc.push.adhoc.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.nd.android.adhoc.basic.log.Logger;
import com.nd.android.adhoc.basic.util.system.AdhocDeviceUtil;

public class PushStoredDeviceInfoUtils {

    private static final String TAG = "PushUtils";
    private static String mDeviceMac = "";

    // 这个mac是取来给Push当 push id用的，先取有线的，再取无线的，只要取到一个，就存起来
    // 就当作是这台机子的唯一标识了
    public static String getDeviceMac(@NonNull Context pContext){
        if(!TextUtils.isEmpty(mDeviceMac)){
            return mDeviceMac;
        }

        PushSpConfigFactory configFactory = PushSpConfigFactory.getInstance();
        mDeviceMac = configFactory.getDeviceMac(pContext);
        if(!TextUtils.isEmpty(mDeviceMac)){
            Logger.d(TAG, "use cached device mac:"+mDeviceMac);
            return mDeviceMac;
        }

        // 先取Lan Mac，这个是有线网卡的，如果android设备还允许有可插拔的有线网卡，那就认命吧
        // 因为我们曾经在ap7上遇到过无线网卡可更换的情况
        mDeviceMac = AdhocDeviceUtil.getEthernetMac();
        if(!TextUtils.isEmpty(mDeviceMac)){
            Logger.d(TAG, "use device lan mac:"+mDeviceMac);
            configFactory.saveDeviceMac(pContext, mDeviceMac);
            return mDeviceMac;
        }

        // 取不到有线，取wifi Mac
        mDeviceMac = AdhocDeviceUtil.getWifiMac(pContext);
        if(!TextUtils.isEmpty(mDeviceMac)){
            Logger.d(TAG, "use device wifi mac:"+mDeviceMac);
            configFactory.saveDeviceMac(pContext, mDeviceMac);
            return mDeviceMac;
        }

        Log.e(TAG, "lan mac and wifi mac both not found");
        return "";
    }
}
