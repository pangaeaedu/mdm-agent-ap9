package com.nd.adhoc.push.client;


import com.nd.adhoc.push.adhoc.sdk.PushSdkModule;

public class libpushclient {

    static {
        try {
            new Exception("lib push client debug").printStackTrace();
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

    // 设置备用服务器的前缀
    public static native void native_pushConfig(String config);

    // 判断当前是否在使用备用服务器
    public static native boolean native_pushIsUsingAlternativeServer();

    // 设置厂商推送通道（在PushLogin之前调用）
    public static native void native_setMFChannel(String packageName, String manufactorName, String manufactorToken);

    // 设置负载均衡服务
    public static native void native_pushSetLoadBalancer(String host);

    // 设置默认服务地址
    public static native void native_pushSetDefaultServerAddr(String ip, int port);

    // 设置心跳时间
    public static native void native_pushSetServerOptions(int offlineTimeoutsec, int retryIntervalSec, int retryCount, int deadTimeouotSec);

    // 开始接收Push消息
    public static native void native_pushLogin(String appId, String mAppKey, String manuFactor, String imei, String mac, String androidId, int mReconnectInterval);

    // 停止接收Push消息
    public static native void native_pushDisconnect();

    // 发送上行消息
    public static native int native_sendUpStreamMsg(String msgid, long ttlSeconds, String contentType, String content, String extraHeaders);

    //
    public static native void native_pushSetReconnectPolicy(boolean reconnectOnKicked);

    // 标记消息已读
    public static native void native_pushAckMsg(long msgId);

    // 发布主题消息
    public static native int native_pushPublishMsgReq(String msgid, String topic, int qos, String content);

    // 订阅主题
    public static native void native_pushSubscribe(String[] topics, int[] qos, int arrayLen);

    // 取消订阅主题
    public static native void native_pushUnsubscribe(String[] topics, int arrayLen);

    // 取消订阅所有主题
    public static native void native_pushUnsubscribeAll();

    // 给本设备打标签
    public static native void native_pushAddTags(String[] keys, String[] values, int arrayLen);

    // 给本设备删除标签
    public static native void native_pushRemoveTags(String[] keys, int arrayLen);

    // 删除本设备所有标签
    public static native void native_pushRemoveAllTags();

    // 设置别名，最多可能会阻塞1秒，注意该请求如果失败，会在下次登陆成功后重试，但程序重启后不会
    public static native int native_pushSetAlias(String alias);

    // 设置本设备的影子
    public static native int native_pushReportShadow(int mode, String reported);

    // 获取本设备的影子
    public static native void native_pushGetShadow(int mode, int version);

    // 删除本设备的影子
    public static native void native_pushDeleteShadow(int mode);

    // Jni初始化
    private static native void native_class_init();

    public static void onPushDeviceToken(String deviceToken) {
        PushSdkModule.getInstance().notifyDeviceToken(deviceToken);
    }

    public static void onPushMessage(String appId, int msgtype, byte[] contenttype, long msgid, long msgTime, String topic, byte[] data, String []extraKeys, String []extarValues) {
        PushSdkModule.getInstance().notifyPushMessage(appId, msgtype, contenttype, msgid, msgTime, topic, data, extraKeys, extarValues);
    }

    public static void onPushUpstreamSent(String msgid, int errCode) {
        PushSdkModule.getInstance().notifyPushUpstreamSent(msgid, errCode);
    }

    public static void onPushShadowUpdated(int mode, String shadow) {
        //PushSdkModule.getInstance().notifyPushShadowUpdated(PushShadowMode.fromIntValue(mode), shadow);
    }

    public static void onPushLoginResult(String appId, int errCode, String errMsg) {
        PushSdkModule.getInstance().notifyClientConnectStatus(errCode==0);
    }

    public static void onPushDisconnected() {
        PushSdkModule.getInstance().notifyClientConnectStatus(false);
    }
}

