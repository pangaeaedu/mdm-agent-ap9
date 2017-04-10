package com.nd.adhoc.push.pushsdk;


public interface PushSdkCallback {
    byte[] onPushMessage(String appid, final byte[] content);
}
