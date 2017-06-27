package com.nd.adhoc.push;


public interface PushSdkCallback {
    byte[] onPushMessage(String appid, final byte[] content);

    /**
     * 客户端与push服务器连接状态回调
     * @param isConnected  是否连接成功
     */
    void onClientConnected(boolean isConnected);
}
