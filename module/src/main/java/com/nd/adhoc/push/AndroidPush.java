package com.nd.adhoc.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.TelephonyManager;

import com.nd.adhoc.push.client.libpushclient;
import com.nd.adhoc.push.pushsdk.PushSdk;
import com.nd.adhoc.push.pushsdk.PushSdkCallback;
import com.nd.adhoc.push.util.DeviceUtil;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * im sync
 */
public class AndroidPush {

    private static AndroidPush inst = new AndroidPush();

    public static AndroidPush getInst() {
        return inst;
    }

    private PushSdk pushSdk;
    private static String deviceId = null;
    BroadcastReceiver receiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    pushSdk.restartPushSdk();
                }
            };

    AtomicBoolean started = new AtomicBoolean(false);

    public synchronized void startPushSdk(final Context context, String ip, int port, final PushSdkCallback callback) {
        pushSdk = new PushSdk(ip, port);

        new Thread(new Runnable() {
            @Override
            public void run() {
                pushSdk.addPushSdkCallback(callback);
                if (started.get()) {
                    return;
                }
                started.set(true);
                IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                context.registerReceiver(receiver, filter);

                final String deviceId = DeviceUtil.generateDeviceId(context);
                final String packageName = context.getPackageName();
                final String appId = "mdm";
                File sdCard = Environment.getExternalStorageDirectory();
                if (null==sdCard){
                    sdCard = Environment.getDownloadCacheDirectory();
                }
                if (null!=sdCard) {
                    String logpath = sdCard + "/" + packageName + "/adhoclog/";
                    libpushclient.native_pushInit(logpath);
                }

                pushSdk.startPushSdk(deviceId, appId);
                AndroidPush.deviceId = pushSdk.getDeviceid();
            }
        }).start();
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public synchronized boolean isConnected() {
        if (pushSdk!=null) {
            return pushSdk.isConnected();
        }
        return false;
    }

    public synchronized PushSdk getPushSdk() {
        return pushSdk;
    }
}
