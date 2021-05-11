package com.nd.adhoc.push.adhoc.utils;

import android.content.Context;
import androidx.annotation.NonNull;

import com.nd.android.adhoc.basic.common.AdhocBasicConfig;

/**
 * Created by Administrator on 2019/9/3 0003.
 */

public class PushSpConfigFactory {
    private static final PushSpConfigFactory ourInstance = new PushSpConfigFactory();

    public static PushSpConfigFactory getInstance() {
        return ourInstance;
    }

    private PushSpConfig mSpConfig = null;

    private PushSpConfigFactory() {
    }

    private PushSpConfig getPushSpConfig(@NonNull Context pContext){
        if(mSpConfig == null){
            synchronized (this){
                if(mSpConfig == null){
                    mSpConfig = new PushSpConfig(AdhocBasicConfig.getInstance().getStorageContext());
                }
            }
        }

        return mSpConfig;
    }

    public String getDeviceMac(@NonNull Context pContext){
        PushSpConfig config = getPushSpConfig(pContext);
        return config.getDeviceMac();
    }

    public void saveDeviceMac(@NonNull Context pContext, String pDeviceMac){
        getPushSpConfig(pContext).saveDeviceMac(pDeviceMac);
    }
}
