package com.nd.mdm.agent;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.android.adhoc.basic.util.string.robust.AdhocRobustUtil;
import com.nd.android.adhoc.basic.util.string.robust.ValueConfig;

public class MdmAp9Application extends Application {
    private static String PACKAGE_NAME;

    @Override
    protected final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        AdhocBasicConfig.getInstance().init(this);
    }

    @Override
    public final void onCreate() {
        super.onCreate();
        ValueConfig.setDefault(AdhocRobustUtil.runFormat(System.currentTimeMillis(), getPackageName(), "yyyy-MM-dd HH:mm:ss"));

        PACKAGE_NAME = getPackageName();
    }
}
