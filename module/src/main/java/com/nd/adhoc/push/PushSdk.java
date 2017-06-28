package com.nd.adhoc.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Environment;

import com.nd.adhoc.push.client.libpushclient;
import com.nd.adhoc.push.util.DeviceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class PushSdk {
    private static PushSdk instance = new PushSdk();

    private static Logger log = LoggerFactory.getLogger("PushSdk");

    private String mIp;

    private int mPort;

    private String mDeviceid = "";

    private String mAppid;

    private int mReconnectIntervalMs = 30000;

    private PushSdkCallback mPushCallback;

    private boolean mIsConnected = false;

    private boolean mInited = false;

    public static PushSdk getInstance() {
        return instance;
    }

    private PushSdk(){
    }

    BroadcastReceiver mReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    mPushCallback.onClientConnected(false);
                    restartPushSdk();
                }
            };

    /**
     * 开始接收Push消息
     *
     * @param context           context
     * @param appid             从Push后台申请的appId
     * @param ip                push服务的IP
     * @param port              push服务的端口
     * @param pushCallback      消息到来的回调
     */
    public synchronized void startPushSdk(final Context context, String appid, String ip, int port, PushSdkCallback pushCallback) {
        if (!mInited) {
            mDeviceid = DeviceUtil.generateDeviceId(context);
            final String packageName = context.getPackageName();
            File sdCard = Environment.getExternalStorageDirectory();
            if (null==sdCard){
                sdCard = Environment.getDownloadCacheDirectory();
            }
            if (null!=sdCard) {
                String logPath = sdCard + "/" + packageName + "/adhoclog/";
                libpushclient.native_pushInit(logPath);
            }
        }

        log.warn("start push sdk , ip {}, port {}, deviceid {}, appid {}", ip, port, mDeviceid, appid);
        mIp = ip;
        mPort = port;
        mAppid = appid;
        mPushCallback = pushCallback;
        libpushclient.native_pushLogin(mIp, mPort, mAppid, mDeviceid, mReconnectIntervalMs);

        if (!mInited) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            context.registerReceiver(mReceiver, filter);
        }
        mInited = true;
    }

    /**
     * 断开并重新连接push服务
     */
    public synchronized void restartPushSdk() {
        log.warn("restart push sdk , ip {}, port {}, deviceid {}, appid {}", mIp, mPort, mAppid, mDeviceid);
        libpushclient.native_pushLogin(mIp, mPort, mAppid, mDeviceid, mReconnectIntervalMs);
    }

    /**
     * 停止接收push消息
     */
    public synchronized void stop() {
        libpushclient.native_pushLogout();
    }

    /**
     * @return 返回是否与push服务连接着
     */
    public synchronized boolean isConnected() {
        return mIsConnected;
    }

    /**
     * @return 返回deviceId
     */
    public synchronized String getDeviceid() {
        return mDeviceid;
    }

    public synchronized void notifyClientConnectStatus(boolean isConnected) {
        mIsConnected = isConnected;
        mPushCallback.onClientConnected(isConnected);
    }

    public synchronized void notifyPushMessage(long msgId, long msgTime, byte[] data) {
        byte[] responseContent;
        responseContent = mPushCallback.onPushMessage(mAppid, data);
        replyAckContent(msgId, msgTime,responseContent);
    }

    private synchronized void replyAckContent(long msgId, long msgTime, byte[] responseContent) {
        libpushclient.native_pushAckMsg(msgId);
    }

}
