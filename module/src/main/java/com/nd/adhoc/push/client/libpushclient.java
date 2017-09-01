package com.nd.adhoc.push.client;

import com.nd.adhoc.push.module.PushSdkModule;

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
    public static native void native_pushLogin(String ip, int port, String appId, String manuFactor, String imei, String mac, String androidId, int mReconnectInterval);

    // 停止接收Push消息
    public static native void native_pushDisconnect();

    // 标记消息已读
    public static native void native_pushAckMsg(long msgId);

    // Jni初始化
    private static native void native_class_init();

    public static void onPushDeviceToken(String deviceToken) {
        PushSdkModule.getInstance().notifyDeviceToken(deviceToken);
    }

    public static void onPushMessage(String appId, long msgid, long msgTime, byte[] data) {
        PushSdkModule.getInstance().notifyPushMessage(msgid, msgTime, data);
    }

    public static void onPushLoginResult(String appId, int errCode, String errMsg) {
        PushSdkModule.getInstance().notifyClientConnectStatus(errCode==0);
    }

    public static void onPushDisconnected() {
        PushSdkModule.getInstance().notifyClientConnectStatus(false);
    }
}

