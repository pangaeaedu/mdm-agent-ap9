package com.nd.adhoc.push.module;

import android.content.Context;
import android.os.Environment;

import com.nd.adhoc.push.client.libpushclient;
import com.nd.adhoc.push.util.DeviceUtil;
import com.nd.sdp.adhoc.push.IPushSdkCallback;

import org.apache.log4j.Logger;

import java.io.File;

/**
 * Created by XWQ on 2017/8/29 0029.
 */

public class PushSdkModule {
    private static Logger log = Logger.getLogger(PushSdkModule.class.getSimpleName());
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
    private void doStartPushSdk(final Context context, String appid, String ip, int port, IPushSdkCallback pushCallback) {
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

            mManufactor = DeviceUtil.getManufactorer();
            mImei = DeviceUtil.getImei(context);
            if (null==mImei) {
                mImei = pseudoId;
            }
            mMac = DeviceUtil.getMac(context);
            mAndroidId = DeviceUtil.getAndroidId(context);
        }
        log.warn("start push sdk" +
                 " , ip = " + ip +
                 " , port = " + port +
                 " , appid = " + appid +
                 " , manufactorer = " + mManufactor +
                 " , imei = " + mImei +
                 " , mac = " + mMac +
                 " , androidid = " + mAndroidId);
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
        doStartPushSdk(context, appid, ip, port, pushCallback);
    }

    /**
     * 设置负载均衡服务
     *
     * @param host      负载均衡服务地址
     * @param port      负载均衡服务端口
     */
    public void setLoadBalancer(String host, int port) {
        log.info("setLoadBalancer( "+ host + " , " + port + " ) ");
        libpushclient.native_pushSetLoadBalancer(host, port);
    }

    /**
     * 断开并重新连接push服务
     */
    public void restartPushSdk() {
        log.warn("restart push sdk" +
                " , ip = " + mIp +
                " , port = " + mPort +
                " , appid = " + mAppid +
                " , manufactorer = " + mManufactor +
                " , imei = " + mImei +
                " , mac = " + mMac +
                " , androidid = " + mAndroidId);
        doNotifyClientConnectStatus(false);
        if (mIp == null || mIp.isEmpty()) {
            log.warn("Ip is null");
        } else if (mPort <= 0) {
            log.warn("Port is wrong. Port = " + mPort);
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
        log.info("stop()");
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
        log.warn("doNotifyClientConnectStatus" +
                 " , currentStatus = " + mIsConnected +
                 " , newStatus  = " +isConnected);
        if (isConnected != mIsConnected || mIsFirst) {
            mIsFirst = false;
            mIsConnected = isConnected;
            if (mPushCallback != null) {
                try {
                    mPushCallback.onPushStatus(isConnected);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void notifyDeviceToken(String deviceToken) {
        log.info("notifyDeviceToken deviceToken = " + deviceToken);
        mDevicetoken = deviceToken;
        if (mPushCallback != null) {
            try {
                mPushCallback.onPushDeviceToken(mDevicetoken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyPushMessage(String appId, int msgtype, byte[] contenttype, long msgid, long msgTime, byte[] data, String []extraKeys, String []extraValues)  {
        if (mPushCallback != null) {
            try {
                mPushCallback.onPushMessage(mAppid, msgtype, contenttype, msgid, msgTime, data, extraKeys, extraValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
