// IPushSdkCallback.aidl
package com.nd.sdp.adhoc.push;

// Declare any non-default types here with import statements

interface IPushSdkCallback {
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
    byte[] onPushMessage(String appId, int msgtype, inout byte[] contenttype, long msgid, long msgTime, inout byte[] data, inout String []extraKeys, inout String []extraValues);

    /**
     * 上行消息发送完成通知
     *
     * @param msgId
     * @param content
     */
    void onPushUpstreamSent(String msgId, int errCode);


    /**
     * 客户端与push服务器连接状态回调
     * @param isConnected  是否连接成功
     */
    void onPushStatus(boolean isConnected);

   /**
     * Push 服務被系統回收, 需要上層從新 start
     */
    void onPushShutdown();
}
