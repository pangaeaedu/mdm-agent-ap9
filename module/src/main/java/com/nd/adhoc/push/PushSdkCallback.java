package com.nd.adhoc.push;


public interface PushSdkCallback {
    /**
     * 获取到Push的DeviceToken后， 回调上层
     *
     * @param deviceToken
     */
    void onPushDeviceToken(String deviceToken);

    /**
     * Push消息通知
     *
     * @param appid
     * @param content
     * @return
     */
    byte[] onPushMessage(String appid, final byte[] content);

    /**
     * 客户端与push服务器连接状态回调
     * @param isConnected  是否连接成功
     */
    void onPushStatus(boolean isConnected);
}
