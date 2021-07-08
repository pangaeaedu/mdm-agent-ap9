package com.nd.adhoc.push.adhoc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.text.TextUtils;

public class PushSpConfig {
    private String mSpName = "";
    private SharedPreferences mPreferences = null;
    private Context mContext = null;

    private static final String KEY_DEVICE_MAC = "device_mac";

    public PushSpConfig(@NonNull Context pContext){
        mContext = pContext;
        mSpName = "adhoc_push_config";
    }

    public String getDeviceMac() {
        return getString(KEY_DEVICE_MAC);
    }

    public void saveDeviceMac(String pMac) {
        saveString(KEY_DEVICE_MAC, pMac);
    }


    public void saveString(String key,String value){
        if (TextUtils.isEmpty(key)){
            return;
        }
        getDefault().edit().putString(key,value).apply();
    }

    public String getString(String key){
        if (TextUtils.isEmpty(key)){
            return "";
        }
        return getDefault().getString(key,"");
    }

    private SharedPreferences getDefault(){
        if(mPreferences == null){
            synchronized (this) {
                if (mContext == null) {
                    throw new RuntimeException("can not get share preference with null " +
                            "context");
                }

                mPreferences = mContext.getSharedPreferences(mSpName, Context.MODE_PRIVATE);
            }
        }

        return mPreferences;
    }
}
