// IPushService.aidl
package com.nd.sdp.adhoc.push;

import com.nd.sdp.adhoc.push.IPushSdkCallback;

interface IPushService {
    /**
     * 设置负载均衡服务
     */
     void setLoadbalancer(String host, int port);

    /**
     * 开始接收Push消息
     *
     * @param context           context
     * @param appid             从Push后台申请的appId
     * @param loadbalancehost   负载均衡服务地址
     * @param loadbalanceport   负载均衡服务端口
     * @param ip                push服务的IP
     * @param port              push服务的端口
     * @param pushCallback 消息到来的回调
     */
    void startPushSdk(String appid, String loadbalancehost, int loadbalanceport, String ip, int port, IPushSdkCallback pushCallback);

    /**
     * 断开并重新连接push服务
     */
    void restartPushSdk();

    /**
     * 停止接收push消息
     */
    void stop();

    /**
     * @return 返回是否与push服务连接着
     */
    boolean isConnected();

    /**
     * @return 返回deviceId
     */
    String getDeviceid();
}
