package com.nd.adhoc.push.client;


import com.nd.adhoc.push.PushSdk;

public class libpushclient {

    static {
        try {
            System.loadLibrary("push_client");
        } catch (UnsatisfiedLinkError ule) {
          ule.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }
        native_class_init();
    }

    // 初始化push， 日志等
    public static native void native_pushInit(String logPath);

    // 开始接收Push消息
    public static native void native_pushLogin(String ip, int port, String appId, String deviceId, int timeoutMs);

    // 停止接收Push消息
    public static native void native_pushLogout();

    // 标记消息已读
    public static native void native_pushAckMsg(long msgId);

    // Jni初始化
    private static native void native_class_init();

    public static void onPushMessage(long msgid, long msgTime, byte[] data) {
        PushSdk.getInstance().notifyPushMessage(msgid, msgTime, data);
    }

    public static void onPushLoginResult(int errCode, String errMsg) {
        PushSdk.getInstance().notifyClientConnectStatus(errCode==0);
    }

    public static void onPushLoggedOut() {
        PushSdk.getInstance().notifyClientConnectStatus(false);
    }
}

