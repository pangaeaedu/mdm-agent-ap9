package com.nd.adhoc.push.pushsdk;

import com.nd.adhoc.push.client.libpushclient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PushSdk {
    private static Logger log = LoggerFactory.getLogger("PushSdk");

    private String mIp;

    private int mPort;

    private String mDeviceid;

    private String mAppid;

    private int mReconnectIntervalMs = 30000;

    private List<PushSdkCallback> mPushCallbackList = new ArrayList<>();

    private AtomicBoolean mIsConnected = new AtomicBoolean(false);

    public PushSdk(String ip, int port){
        mIp = ip;
        mPort = port;
    }

    /**
     * push消息回调接口
     * 需要注册推送的组件需要将自己的callback
     * 注册进来
     * @param callback
     */
    public void addPushSdkCallback(PushSdkCallback callback) {
        mPushCallbackList.add(callback);
    }

    public void startPushSdk(String deviceid, String appid) {
        synchronized (this) {
            log.warn("start push sdk , deviceid {}, appid {}", deviceid, appid);
            mDeviceid = deviceid;
            mAppid = appid;
        }
        libpushclient.native_pushLogin(mIp, mPort, mAppid, mDeviceid, mReconnectIntervalMs);
    }

    public String getDeviceid() {
        return mDeviceid;
    }

    public void restartPushSdk() {
        log.warn("restart push sdk , deviceid {}, appid {} ", mDeviceid, mAppid);
        libpushclient.native_pushLogin(mIp, mPort, mAppid, mDeviceid, mReconnectIntervalMs);
    }

    public void stop() {
        libpushclient.native_pushLogout();
    }

    public void notifyClientConnectStatus(boolean isConnected) {
        mIsConnected.set(isConnected);
        for (PushSdkCallback callback : mPushCallbackList) {
            callback.onClientConnected(isConnected);
        }
    }

    public void notifyPushMessage(long msgId, long msgTime, byte[] data) {
        byte[] responseContent;
        for (PushSdkCallback callback : mPushCallbackList) {
            responseContent = callback.onPushMessage(mAppid, data);
            replyAckContent(msgId, msgTime,responseContent);
        }
    }

    /**
     * 回复确认消息
     * @param msgId
     * @param msgTime
     * @param responseContent
     */
    private void replyAckContent(long msgId, long msgTime, byte[] responseContent) {
        libpushclient.native_pushAckMsg(msgId);
    }

    public boolean isConnected() {
        return mIsConnected.get();
    }

}
