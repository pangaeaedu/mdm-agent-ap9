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
    byte[] onPushMessage(String appId, int msgtype, inout byte[] contenttype, long msgid, long msgTime, String topic, inout byte[] data, inout String []extraKeys, inout String []extraValues);

    /**
     * 客户端与push服务器连接状态回调
     * @param isConnected  是否连接成功
     */
    void onPushStatus(boolean isConnected);

   /**
     * Push 服務被系統回收, 需要上層從新 start
     */
    void onPushShutdown();

    /**
     * 上行消息发送完成通知
     *
     * @param msgId
     * @param content
     */
    void onPushUpstreamSent(String msgId, int errCode);

    /**
     * 通知本设备的影子更新
     *
     * @param mode 参考 @PushShadowMode
     *             ShadowModeDevice(0), // 以设备ID作为影子记录ID
     *             SHadowModeAlias(1);  // 以别名作为影子记录ID
     * @param document 完整的影子信息，json格式
     *                 格式见wiki https://dwz.cn/psTR5YYP
     */
    void onPushShadowUpdated(int mode, String document);
}
