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

    // 从服务端获取
    private String mDevicetoken;

    private String mAppid;

    private String mManufactor;

    private String mImei;

    private String mMac;

    private String mAndroidId;

    private int mReconnectIntervalMs = 30000;

    private PushSdkCallback mPushCallback;

    private boolean mIsConnected = false;

    private boolean mIsFirst = true;

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
    private void doStartPushSdk(final Context context, String appid, String deviceid, String ip, int port, PushSdkCallback pushCallback) {
        if (!mInited) {
            final String packageName = context.getPackageName();
            File sdCard = Environment.getExternalStorageDirectory();
            if (null==sdCard){
                sdCard = Environment.getDownloadCacheDirectory();
            }
            if (null!=sdCard) {
                String logPath = sdCard + "/" + packageName + "/adhoclog/";
                libpushclient.native_pushInit(logPath);
            }

            String pseudoId = DeviceUtil.getPseudoId();
            String pseudoIdLong = DeviceUtil.getPseudoIDLong();

            mManufactor = DeviceUtil.getManufactorer();
            mImei = DeviceUtil.getImei(context);
            if (null==mImei) {
                mImei = pseudoId;
            }
            mMac = DeviceUtil.getMac(context);
            mAndroidId = DeviceUtil.getAndroidId(context);
        }
        log.warn("start push sdk , ip {}, port {}, appid {}, manufactorer {}, imei {}, mac {}, androidid {}",
                ip, port, appid, mManufactor, mImei, mMac, mAndroidId);
        mIp = ip;
        mPort = port;
        mAppid = appid;
        mPushCallback = pushCallback;
        libpushclient.native_pushLogin(mIp, mPort, mAppid, mManufactor, mImei, mMac, mAndroidId, mReconnectIntervalMs);

        if (!mInited) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            context.registerReceiver(mReceiver, filter);
        }
        mInited = true;
    }

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
        doStartPushSdk(context, appid, null, ip, port, pushCallback);
    }

    /**
     * 断开并重新连接push服务
     */
    public synchronized void restartPushSdk() {
        log.warn("restart push sdk , ip {}, port {}, appid {}, manufactorer {}, imei {}, mac {}, androidid {}",
                mIp, mPort, mAppid, mManufactor, mImei, mMac, mAndroidId);
        doNotifyClientConnectStatus(false);
        libpushclient.native_pushLogin(mIp, mPort, mAppid, mManufactor, mImei, mMac, mAndroidId, mReconnectIntervalMs);
    }

    /**
     * 停止接收push消息
     */
    public synchronized void stop() {
        libpushclient.native_pushDisconnect();
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
        return mDevicetoken;
    }

    public synchronized void notifyClientConnectStatus(boolean isConnected) {
        doNotifyClientConnectStatus(isConnected);
    }

    private void doNotifyClientConnectStatus(boolean isConnected) {
        log.warn("doNotifyClientConnectStatus , currentStatus {} , newStatus {}", mIsConnected, isConnected);
        if (isConnected!=mIsConnected || mIsFirst) {
            mIsFirst = false;
            mIsConnected = isConnected;
            mPushCallback.onPushStatus(isConnected);
        }
    }

    public synchronized void notifyDeviceToken(String deviceToken) {
        log.info("notifyDeviceToken {}", deviceToken);
        mDevicetoken = deviceToken;
        mPushCallback.onPushDeviceToken(deviceToken);
    }

    public synchronized void notifyPushMessage(long msgId, long msgTime, byte[] data) {
//        byte[] responseContent;
//        responseContent =
        mPushCallback.onPushMessage(mAppid, data);
        //replyAckContent(msgId, msgTime,responseContent);
    }

//    private synchronized void replyAckContent(long msgId, long msgTime, byte[] responseContent) {
//        libpushclient.native_pushAckMsg(msgId);
//    }

}
