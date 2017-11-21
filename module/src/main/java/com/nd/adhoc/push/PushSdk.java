package com.nd.adhoc.push;

import android.content.Context;

import com.nd.adhoc.push.module.PushSdkModule;
import com.nd.sdp.adhoc.push.IPushSdkCallback;

public class PushSdk {
    private static PushSdk instance = new PushSdk();

    public static PushSdk getInstance() {
        return instance;
    }

    /**
     * 设置负载均衡服务
     *
     * @param host      负载均衡服务地址
     * @param port      负载均衡服务端口
     */
    public synchronized void setLoadBalancer(String host, int port) {
        PushSdkModule.getInstance().setLoadBalancer(host, port);
    }

    /**
     * 开始接收Push消息
     *
     * @param context      context
     * @param appid        从Push后台申请的appId
     * @param ip           push服务的IP
     * @param port         push服务的端口
     * @param pushCallback 消息到来的回调
     */
    public synchronized void startPushSdk(final Context context, String appid, String ip, int port, IPushSdkCallback pushCallback) {
        PushSdkModule.getInstance().startPushSdk(context, appid, ip, port, pushCallback);
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
