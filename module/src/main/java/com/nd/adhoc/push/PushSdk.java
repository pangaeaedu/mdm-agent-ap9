package com.nd.adhoc.push;

import android.app.usage.NetworkStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.nd.adhoc.push.module.PushSdkModule;
import com.nd.sdp.adhoc.push.IPushSdkCallback;

public class PushSdk {
    private static PushSdk instance = new PushSdk();

    public static PushSdk getInstance() {
        return instance;
    }

    private Context context = null;

    /**
     * 设置负载均衡服务
     *
     * @param host 负载均衡服务地址
     * @param port 负载均衡服务端口
     */
    public synchronized void setLoadBalancer(String host, int port) {
        PushSdkModule.getInstance().setLoadBalancer(host, port);
    }

    /**
     * 开始接收Push消息
     *
     * @param context      context
     * @param appid        从Push后台申请的appId
     * @param appKey       从Push后台申请的appKey
     * @param ip           push服务的IP
     * @param port         push服务的端口
     * @param pushCallback 消息到来的回调
     */
    public synchronized void startPushSdk(final Context context, String appid, String appKey, String ip, int port, IPushSdkCallback pushCallback) {
        PushSdkModule.getInstance().startPushSdk(context, appid, appKey, ip, port, pushCallback);
    }

    /**
     * 发送上行消息
     *
     * @param msgid 消息ID
     * @param ttlSeconds 过期时间
     * @param contentType 消息类型
     * @param content 消息内容
     * @return 0 成功
     *         非0 失败
     */
    public synchronized int sendUpStreamMsg(String msgid, long ttlSeconds, String contentType, String content) {
        return PushSdkModule.getInstance().sendUpStreamMsg(msgid, ttlSeconds, contentType, content);
    }

    /**
     * 断开并重新连接push服务
     */
    public synchronized void restartPushSdk() {
        PushSdkModule.getInstance().restartPushSdk();
    }

    /**
     * 停止接收push消息
     */
    public synchronized void stop() {
        PushSdkModule.getInstance().stop();
    }

    /**
     * @return 返回是否与push服务连接着
     */
    public synchronized boolean isConnected() {
        return PushSdkModule.getInstance().isConnected();
    }

    /**
     * @return 返回deviceId
     */
    public synchronized String getDeviceid() {
        return PushSdkModule.getInstance().getDeviceid();
    }
}
