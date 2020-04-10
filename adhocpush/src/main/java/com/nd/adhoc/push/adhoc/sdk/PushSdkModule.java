package com.nd.adhoc.push.adhoc.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.nd.adhoc.push.adhoc.utils.DeviceUtil;
import com.nd.adhoc.push.client.libpushclient;
import com.nd.android.adhoc.basic.util.storage.AdhocStorageUtil;
import com.nd.sdp.adhoc.push.IPushSdkCallback;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.mindpipe.android.logging.log4j.LogConfigurator;


public class PushSdkModule {
    private static Logger log = Logger.getLogger(PushSdkModule.class.getSimpleName());
    private static PushSdkModule instance = new PushSdkModule();

    private static final String TAG = "PushSdkModule";
    private String mIp;

    private int mPort;

    // 从服务端获取
    private String mDevicetoken;

    private String mAppid;

    private String mAppKey;

    private String mManufactor;

    private String mImei;

    private String mMac;

    private String mAndroidId;

    private int mReconnectIntervalMs = 10000;

    private IPushSdkCallback mPushCallback;

    private boolean mIsConnected = false;

    private boolean mIsFirst = true;

    private boolean mInited = false;

    private boolean mIsScheduleStarting = false;

    private long mLastRestartTimestampMs = 0;

    private String packageName = "";

    private String manufactorName = "";

    private String manufactorToken = "";
	
    private boolean mAutoStart = true;

    private static long RESTART_INTERVAL_MS = 5000;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static PushSdkModule getInstance() {
        return instance;
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
    private void doStartPushSdk(final Context context, String appid, String appKey, String ip, int port, IPushSdkCallback pushCallback) {
        if (!mInited) {
//            final String packageName = context.getPackageName();
//            File sdCard = Environment.getExternalStorageDirectory();
//            if (null == sdCard) {
//                sdCard = Environment.getDownloadCacheDirectory();
//            }

            String sdCard = AdhocStorageUtil.getSDCardFilesDir(context);
            if (null != sdCard) {
                String logPath = sdCard + "/log/adhoclog/";
                libpushclient.native_pushInit(logPath);
            }
            String pseudoId = DeviceUtil.getPseudoId();
            mManufactor = DeviceUtil.getManufactorer();
            mImei = DeviceUtil.getImei(context);
            if (null == mImei) {
                mImei = pseudoId;
            }
            mMac = DeviceUtil.getMac(context);
            mAndroidId = DeviceUtil.getAndroidId(context);
        }
        Log.e(TAG,"start push sdk" +
                " , ip = " + ip +
                " , port = " + port +
                " , appid = " + appid +
                " , manufactorer = " + mManufactor +
                " , imei = " + mImei +
                " , mac = " + mMac +
                " , androidid = " + mAndroidId);
        mIp = ip;
        mPort = port;
        mAppid = appid;
        mAppKey = appKey;
        mPushCallback = pushCallback;
        if (null == mAppKey) {
            mAppKey = "";
        }
        mInited = true;
        restartPushSdk();
    }

    /**
     * 发送上行消息
     *
     * @param topic 主题 ，默认为空
     * @param msgid 消息ID
     * @param ttlSeconds 过期时间
     * @param contentType 消息类型
     * @param content 消息内容
     * @return 0 成功
     *         非0 失败
     */
    @SuppressLint("DefaultLocale")
    public int sendUpStreamMsg(String topic, String msgid, long ttlSeconds, String contentType, String content) {
        int ret = libpushclient.native_sendUpStreamMsg(topic, msgid, ttlSeconds, contentType, content);

        if(ret != 0){
            Log.e(TAG, "sendUpStreamMsg failed topic:"+topic+"msgid:"+msgid+" ret:"+ret);
            notifyPushUpstreamSent(msgid, ret);
        }

        return ret;
    }

    /**
     * 发送主题消息
     *
     * @param topic 主题 ，默认为空
     * @param qos 见 PushQoS
     * @param content 消息内容
     * @return 0 成功
     *         非0 失败
     */
    @SuppressLint("DefaultLocale")
    public int publish(String topic, String msgid, PushQoS qos, String content) {
        return libpushclient.native_pushPublishMsgReq(msgid, topic, qos.getIntValue(), content);
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
    @SuppressLint("DefaultLocale")
    public void startPushSdk(final Context context, final String appid, final String appKey, final String ip, final int port, final IPushSdkCallback pushCallback) {
        setupLogConfigurator(context);

        Log.e(TAG,String.format("startPushSdk(appid=%s, appKey=%s, ip=%s, port=%d)", appid, appKey != null ? appKey : "null", ip, port));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,String.format("before run startPushSdk(appid=%s, appKey=%s, ip=%s, port=%d)", appid, appKey != null ? appKey : "null", ip, port));
                doStartPushSdk(context, appid, appKey, ip, port, pushCallback);
                Log.e(TAG,String.format("after run startPushSdk(appid=%s, appKey=%s, ip=%s, port=%d)", appid, appKey != null ? appKey : "null", ip, port));
            }
        });
    }

    private void setupLogConfigurator(Context pContext){
        try {

            String sdCard = AdhocStorageUtil.getSDCardFilesDir(pContext);

            if (TextUtils.isEmpty(sdCard)) {
                LogConfigurator logConfigurator = new LogConfigurator();
                logConfigurator.setRootLevel(Level.ALL);
                logConfigurator.setFilePattern("%d %-5p [%c{2}] %m%n");
                logConfigurator.setMaxFileSize(1024 * 1024 * 50);
                logConfigurator.setMaxBackupSize(5);
                logConfigurator.setImmediateFlush(true);
                logConfigurator.setUseFileAppender(false);
                logConfigurator.setUseLogCatAppender(true);
                logConfigurator.configure();
                log.warn("no sdcard for log");
            } else {
                String logPath = sdCard + "/log/adhoclog/";
                String log4jLogPath = logPath + "push.log";
                LogConfigurator logConfigurator = new LogConfigurator();
                logConfigurator.setFileName(log4jLogPath);
                logConfigurator.setRootLevel(Level.ALL);
                logConfigurator.setFilePattern("%d %-5p [%c{2}] %m%n");
                logConfigurator.setMaxFileSize(1024 * 1024 * 50);
                logConfigurator.setMaxBackupSize(5);
                logConfigurator.setImmediateFlush(true);
                logConfigurator.setUseFileAppender(true);
                logConfigurator.setUseLogCatAppender(true);
                logConfigurator.configure();
                log.warn("logpath " + log4jLogPath);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置厂商推送通道（在PushLogin之前调用）
     *
     * @param packageName 在这个厂商的包名
     * @param manufactorName 厂商名
     * @param manufactorToken 厂商分配的设备Token
     */
    public synchronized void setMFChannel(final String packageName, final String manufactorName, final String manufactorToken) {
        log.info(String.format("setMFChannel(packageName=%s, manufactorName=%s, manufactorToken=%s)", packageName, manufactorName, manufactorToken));
        this.packageName = packageName;
        this.manufactorName = manufactorName;
        this.manufactorToken = manufactorToken;
        executorService.submit(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                log.info(String.format("before run setMFChannel(packageName=%s, manufactorName=%s, manufactorToken=%s)", packageName, manufactorName, manufactorToken));
                libpushclient.native_setMFChannel(packageName, manufactorName, manufactorToken);
                log.info(String.format("after run setMFChannel(packageName=%s, manufactorName=%s, manufactorToken=%s)", packageName, manufactorName, manufactorToken));
            }
        });
    }

    /**
     * 设置负载均衡服务
     *
     * @param url 负载均衡服务地址
     */
    @SuppressLint("DefaultLocale")
    public void setLoadBalancer(final String url) {
        log.info(String.format("setLoadBalancer(url=%s)", url));
        executorService.submit(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                log.info(String.format("before run setLoadBalancer(url=%s)", url));
                libpushclient.native_pushSetLoadBalancer(url);
                log.info(String.format("after run setLoadBalancer(url=%s)", url));
            }
        });
    }

    /**
     * 订阅主题， 注意该请求如果失败，会不断重试
     */
    public void subscribe(Map<String, PushQoS> topics) {
        String[] topicsArray = new String[topics.size()];
        int[] qosArray = new int[topics.size()];
        int index = 0;
        for (Map.Entry<String,PushQoS> topic : topics.entrySet()) {
            topicsArray[index] = topic.getKey();
            qosArray[index] = topic.getValue().getIntValue();
            index++;
        }
        libpushclient.native_pushSubscribe(topicsArray, qosArray, topics.size());
    }

    /**
     * 取消订阅某几个主题， 注意该请求如果失败，会不断重试
     */
    public void unsubscribe(String[] topics) {
        libpushclient.native_pushUnsubscribe(topics, topics.length);
    }

    /**
     * 取消订阅所有主题， 注意该请求如果失败，会不断重试
     */
    public void unsubscribeAll() {
        libpushclient.native_pushUnsubscribeAll();
    }

    /**
     * 增加客户端的标签， 注意该请求如果失败，会不断重试
     *
     * @param tags Map<String,String>  ，比如 <省份,福建>
     */
    public void addTags(Map<String,String> tags) {
        String[] keys = new String[tags.size()];
        String[] values = new String[tags.size()];
        int index = 0;
        for (Map.Entry<String,String> tag : tags.entrySet()) {
            keys[index] = tag.getKey();
            values[index] = tag.getValue();
            index++;
        }
        libpushclient.native_pushAddTags(keys, values, tags.size());
    }

    /**
     * 删除本客户端的标签， 注意该请求如果失败，会不断重试
     *
     * @param keys 需要删除的标签 Key
     */
    public void removeTags(String []keys) {
        libpushclient.native_pushRemoveTags(keys, keys.length);
    }

    /**
     * 删除本客户端的所有标签， 注意该请求如果失败，会不断重试
     */
    public void removeAllTags() {
        libpushclient.native_pushRemoveAllTags();
    }

    /**
     * 设置别名，最多可能会阻塞1秒，注意该请求如果失败，会在下次登陆成功后重试，但程序重启后不会
     *
     * @return  0               表示成功
     *          非零值           错误码, 即使返回错误码， 也有可能成功， 因为等待服务端回应只等待1秒， 服务端仍然有可能成功
     */
    public int setAlias(String alias) {
        return libpushclient.native_pushSetAlias(alias);
    }

    /**
     * 更新影子， 注意该请求如果失败，会不断重试
     */
    public int reportShadow(PushShadowMode mode, String reported) {
        return libpushclient.native_pushReportShadow(mode.getIntValue(), reported);
    }

    /**
     * 获取影子，获取后通过 OnPushShadowUpdated 函数返回
     *
     * @param version 本地拥有的版本， 不清楚可以传0
     */
    public synchronized void getShadow(PushShadowMode mode, int version) {
        libpushclient.native_pushGetShadow(mode.getIntValue(), version);
    }

    /**
     * 删除影子， 注意该请求如果失败，会不断重试
     */
    public synchronized void deleteShadow(PushShadowMode mode) {
        libpushclient.native_pushDeleteShadow(mode.getIntValue());
    }

    /**
     * 断开并重新连接push服务
     */
    public void restartPushSdk() {
        log.info("restartPushSdk , version = 0.3.23");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                long lastRestartInterval = SystemClock.elapsedRealtime()-mLastRestartTimestampMs;
                long scheduleInterval = RESTART_INTERVAL_MS-lastRestartInterval;
                log.info("restartPushSdk , scheduleInterval:"+scheduleInterval);
                if (scheduleInterval>0) {
                    if (mIsScheduleStarting) {
                        log.info("restartPushSdk ignored , already scheduled");
                        return;
                    }
                    mIsScheduleStarting = true;
                    log.info("restartPushSdk schedule after " + scheduleInterval + " ms" );
                    executorService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            doRestartPushSdk();
                        }
                    },scheduleInterval, TimeUnit.MILLISECONDS);
                } else {
                    doRestartPushSdk();
                }
            }
        });
    }

    public void setAutoStart(boolean pStart){
        Log.e(TAG, "setAutoStart:"+pStart);
        mAutoStart = pStart;
    }

    private void doRestartPushSdk() {
        Log.e(TAG,"before run restartPushSdk");
        mIsScheduleStarting = false;
        mLastRestartTimestampMs = SystemClock.elapsedRealtime();
        Log.e(TAG,"restart push sdk" +
                " , ip = " + mIp +
                " , port = " + mPort +
                " , appid = " + mAppid +
                " , manufactorer = " + mManufactor +
                " , imei = " + mImei +
                " , mac = " + mMac +
                " , androidid = " + mAndroidId);
        doNotifyClientConnectStatus(false);
        boolean needstart = false;
        if (mIp == null || mIp.isEmpty()) {
            Log.e(TAG,"Ip is null");
        } else if (mPort <= 0) {
            Log.e(TAG,"Port is wrong. Port = " + mPort);
        } else if (mAppid == null || mAppid.isEmpty()) {
            Log.e(TAG,"App id is null");
        } else {
            if (mManufactor == null || mManufactor.isEmpty()) {
                Log.e(TAG,"Manufactor is null");
                mManufactor = "";
            }
            if (mImei == null || mImei.isEmpty()) {
                Log.e(TAG,"Imei is null");
                mImei = "";
            } else {
                needstart = true;
            }
            if (mMac == null || mMac.isEmpty()) {
                Log.e(TAG,"Mac is null");
                mMac = "";
            } else {
                needstart = true;
            }
            if (mAndroidId == null || mAndroidId.isEmpty()) {
                Log.e(TAG,"AndroidId is null");
                mAndroidId = "";
            } else {
                needstart = true;
            }
        }
        if (null == mAppKey) {
            mAppKey = "";
        }
        if (needstart) {
            if (mAutoStart) {
                Log.d(TAG, "native_pushLogin start with IP:" + mIp + " port:" + mPort
                        + " appID:" + mAppid + " appKey:" + mAppKey + " manufactor:" + mManufactor
                        + " imei:" + mImei + " mac:" + mMac);
                libpushclient.native_pushLogin(mIp, mPort, mAppid, mAppKey, mManufactor, mImei,
                        mMac, "", mReconnectIntervalMs);
                Log.e(TAG, "after run restartPushSdk");
            } else {
                Log.d(TAG, "auto start is false, not calling native_pushLogin");
            }
        } else {
            Log.e(TAG,"retry restartPushSdk");
            restartPushSdk();
        }
    }

    /**
     * 停止接收push消息
     */
    public void stop() {
        Log.e(TAG,"stop()");
        mIsConnected = false;
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,"before run stop()");
                libpushclient.native_pushDisconnect();
                Log.e(TAG,"after run stop()");
            }
        });

    }

    /**
     * @return 返回是否与push服务连接着
     */
    public boolean isConnected() {
        return mIsConnected;
    }

    /**
     * @return 返回deviceId
     */
    public String getDeviceid() {
        return mDevicetoken;
    }

    public void notifyClientConnectStatus(final boolean isConnected) {
        Log.e(TAG, String.format("notifyClientConnectStatus(%b)", isConnected));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,String.format("before run notifyClientConnectStatus(%b)", isConnected));
                doNotifyClientConnectStatus(isConnected);
                Log.e(TAG,String.format("after run notifyClientConnectStatus(%b)", isConnected));
            }
        });

    }

    private void doNotifyClientConnectStatus(final boolean isConnected) {
        Log.e(TAG,String.format("doNotifyClientConnectStatus(%b)", isConnected));

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,String.format("before run doNotifyClientConnectStatus(%b)", isConnected));

                Log.e(TAG,"doNotifyClientConnectStatus" +
                        " , currentStatus = " + mIsConnected +
                        " , newStatus  = " + isConnected);
                if (isConnected != mIsConnected || mIsFirst) {
                    mIsFirst = false;
                    mIsConnected = isConnected;
                    if (mPushCallback != null) {
                        try {
                            mPushCallback.onPushStatus(isConnected);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                Log.e(TAG,String.format("after run doNotifyClientConnectStatus(%b)", isConnected));
            }
        });

    }

    public void notifyDeviceToken(final String deviceToken) {
        Log.e(TAG,"notifyDeviceToken(deviceToken = " + deviceToken + ")");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,"before run notifyDeviceToken(deviceToken = " + deviceToken + ")");
                mDevicetoken = deviceToken;
                if (mPushCallback != null) {
                    try {
                        mPushCallback.onPushDeviceToken(mDevicetoken);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.e(TAG,"after run notifyDeviceToken(deviceToken = " + deviceToken + ")");
            }
        });
    }

    @SuppressLint("DefaultLocale")
    public void notifyPushMessage(final String appId, final int msgtype, final byte[] contenttype, final long msgid, final long msgTime, final byte[] data, final String[] extraKeys, final String[] extraValues) {
        Log.e(TAG,String.format("notifyPushMessage(appid=%s, msgtype=%d, msgid=%d, msgtime=%d)", appId, msgtype, msgid, msgTime));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,String.format("before run notifyPushMessage(appid=%s, msgtype=%d, msgid=%d, msgtime=%d)", appId, msgtype, msgid, msgTime));
                if (mPushCallback != null) {
                    try {
                        mPushCallback.onPushMessage(mAppid, msgtype, contenttype, msgid, msgTime, data, extraKeys, extraValues);
                    } catch (Exception e) {
                        Log.e(TAG, "process push message error:"+e.toString());
                    }
                }
                Log.e(TAG,String.format("after run notifyPushMessage(appid=%s, msgtype=%d, msgid=%d, msgtime=%d)", appId, msgtype, msgid, msgTime));
            }
        });
    }


    public void notifyPushUpstreamSent(String pMsgID, int pErrorCode){
        Log.e(TAG,String.format("notifyPushUpstreamSent(pMsgID=%s, pErrorCode=%d)", pMsgID, pErrorCode));
        if (mPushCallback != null) {
            try {
                mPushCallback.notifyMessageSentResult(pMsgID, pErrorCode);
            } catch (Exception e) {
                Log.e(TAG, "notifyMessageSentResult message error:"+e.toString());
            }
        }
    }

}
