package com.nd.adhoc.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.nd.adhoc.push.pushsdk.PushSdk;
import com.nd.adhoc.push.pushsdk.PushSdkCallback;
import com.nd.adhoc.push.util.DeviceUtil;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * im sync
 */
public class AndroidPush {

    private static AndroidPush inst = new AndroidPush();

    public static AndroidPush getInst() {
        return inst;
    }

    private PushSdk pushSdk = new PushSdk();
    private static String deviceId = null;
    BroadcastReceiver receiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    pushSdk.restartPushSdk();
                }
            };

    AtomicBoolean started = new AtomicBoolean(false);

    public void startPushSdk(final Context context, final PushSdkCallback callback) {

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
                final String packageName = context.getPackageName().replace(".", "_");
                pushSdk.startPushSdk(deviceId, packageName);
                AndroidPush.deviceId = pushSdk.getDeviceid();
            }
        }).start();
    }

    public static String getDeviceId() {
        return deviceId;
    }
}
