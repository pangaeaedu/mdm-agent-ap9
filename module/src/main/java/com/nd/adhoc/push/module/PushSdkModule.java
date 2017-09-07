package com.nd.adhoc.push.module;

import android.content.Context;
import android.os.Environment;

import com.nd.adhoc.push.client.libpushclient;
import com.nd.adhoc.push.util.DeviceUtil;
import com.nd.sdp.adhoc.push.IPushSdkCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by XWQ on 2017/8/29 0029.
 */

public class PushSdkModule {
    private static Logger log = LoggerFactory.getLogger(PushSdkModule.class.getSimpleName());
    private static PushSdkModule instance = new PushSdkModule();

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

    private IPushSdkCallback mPushCallback;

    private boolean mIsConnected = false;

    private boolean mIsFirst = true;

    private boolean mInited = false;

    public static PushSdkModule getInstance() {
        return instance;
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
    private void doStartPushSdk(final Context context, String appid, String deviceid, String ip, int port, IPushSdkCallback pushCallback) {
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
    public void startPushSdk(final Context context, String appid, String ip, int port, IPushSdkCallback pushCallback) {
        doStartPushSdk(context, appid, null, ip, port, pushCallback);
    }

    /**
     * 设置负载均衡服务
     *
     * @param host      负载均衡服务地址
     * @param port      负载均衡服务端口
     */
    public void setLoadBalancer(String host, int port) {
        libpushclient.native_pushSetLoadBalancer(host, port);
    }

    /**
     * 断开并重新连接push服务
     */
    public void restartPushSdk() {
        log.warn("restart push sdk , ip {}, port {}, appid {}, manufactorer {}, imei {}, mac {}, androidid {}",
                mIp, mPort, mAppid, mManufactor, mImei, mMac, mAndroidId);
        doNotifyClientConnectStatus(false);
        if (mIp == null || mIp.isEmpty()) {
            log.warn("Ip is null");
        } else if (mPort <= 0) {
            log.warn("Port is wrong. Port = {}", mPort);
        } else if (mAppid == null || mAppid.isEmpty()) {
            log.warn("App id is null");
        } else if (mManufactor == null || mManufactor.isEmpty()) {
            log.warn("Manufactor is null");
        } else if (mImei == null || mImei.isEmpty()) {
            log.warn("Imei is null");
        } else if (mMac == null || mMac.isEmpty()) {
            log.warn("Mac is null");
        } else if (mAndroidId == null || mAndroidId.isEmpty()) {
            log.warn("AndroidId is null");
        } else {
            libpushclient.native_pushLogin(mIp, mPort, mAppid, mManufactor, mImei, mMac, mAndroidId, mReconnectIntervalMs);
        }
    }

    /**
     * 停止接收push消息
     */
    public void stop() {
        mPushCallback = null;
        libpushclient.native_pushDisconnect();
    }

    /**
     * @return 返回是否与push服务连接着
     */
    public boolean isConnected() {
        return mIsConnected;
    }

    /**
     * @return 返回deviceId
     */
    public String getDeviceid() {
        return mDevicetoken;
    }

    public void notifyClientConnectStatus(boolean isConnected) {
        doNotifyClientConnectStatus(isConnected);
    }

    private void doNotifyClientConnectStatus(boolean isConnected) {
        log.warn("doNotifyClientConnectStatus , currentStatus {} , newStatus {}", mIsConnected, isConnected);
        if (isConnected != mIsConnected || mIsFirst) {
            mIsFirst = false;
            mIsConnected = isConnected;
            if (mPushCallback != null) {
                try {
                    mPushCallback.onPushStatus(isConnected);
                } catch (Exception e) {
                    mPushCallback = null;
                    e.printStackTrace();
                }
            }
        }
    }

    public void notifyDeviceToken(String deviceToken) {
        log.info("notifyDeviceToken {}", deviceToken);
        mDevicetoken = deviceToken;
        if (mPushCallback != null) {
            try {
                mPushCallback.onPushDeviceToken(deviceToken);
            } catch (Exception e) {
                mPushCallback = null;
                e.printStackTrace();
            }
        }
    }

    public void notifyPushMessage(long msgId, long msgTime, byte[] data) {
        if (mPushCallback != null) {
            try {
                mPushCallback.onPushMessage(mAppid, data);
            } catch (Exception e) {
                mPushCallback = null;
                e.printStackTrace();
            }
        }
    }

}
