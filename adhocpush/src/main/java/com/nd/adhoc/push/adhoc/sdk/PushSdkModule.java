package com.nd.adhoc.push.adhoc.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.nd.adhoc.push.adhoc.utils.DeviceUtil;
import com.nd.adhoc.push.client.libpushclient;
import com.nd.android.adhoc.basic.util.storage.AdhocStorageAdapter;
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

    private String mLoadbalancer;

    private String mDefaultIp = "";

    private int mDefaultPort = 0;

    // 从服务端获取
    private String mDevicetoken;

    private String mAppid;

    private String mAppKey;

    private String mManufactor;

    private String mImei;

    private String mMac;

    private String mAndroidId;

    private int mReconnectIntervalMs = 10000;

    private int mOfflineTimeoutsec = 0, mRetryIntervalSec = 0, mRetryCount = 0, mDeadTimeouotSec = 0;

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
    private String mAlias;

    public static PushSdkModule getInstance() {
        return instance;
    }

    /**
     * 开始接收Push消息
     *
     * @param context               context
     * @param appid                 从Push后台申请的appId
     * @param serverLbsUrl          服务器地址
     *        开发: http://iot-api.dev.101.com:1770/v5/sdk/access
     *        测试: http://172.24.132.143:8757/v5/sdk/access
     *        预生产: http://iot-api.beta.101.com:1770/v5/sdk/access
     *        生产: http://iot-api.101.com:1770/v5/sdk/access
     *        香港: http://iot-api.hk.101.com:1770/v5/sdk/access
     *        CA: http://iot-api.awsca.101.com:1770/v5/sdk/access
     *        埃及自用演练：http://172.24.132.63:8757/v5/sdk/access
     *        埃及OMO演练：https://omo-mdm-pushlbs.moe.101.com/v5/sdk/access
     *                              
     * @param pushCallback 消息到来的回调
     */
    private void doStartPushSdk(final Context context, String appid, String appKey, String serverLbsUrl, IPushSdkCallback pushCallback) {
        if (!mInited) {
            Log.i(TAG, "doStartPushSdk: init data");
            String sdCard = AdhocStorageAdapter.getFilesDir("log");
            if (null != sdCard) {
                String logPath = sdCard + "adhoclog/";
                libpushclient.native_pushInit(logPath);
                Log.i(TAG, "doStartPushSdk: init log path:" + logPath);
            }
            String pseudoId = DeviceUtil.getPseudoId();
            Log.i(TAG, "doStartPushSdk: init pseudoId:" + pseudoId);
            mManufactor = DeviceUtil.getManufactorer();
            Log.i(TAG, "doStartPushSdk: init Manufactorer:" + mManufactor);
            mImei = DeviceUtil.getImei(context);
            if (null == mImei) {
                mImei = pseudoId;
            }
            Log.i(TAG, "doStartPushSdk: init imei:" + mImei);
            mMac = DeviceUtil.getMac(context);
            Log.i(TAG, "doStartPushSdk: init mac:" + mMac);
            mAndroidId = DeviceUtil.getAndroidId(context);
            Log.i(TAG, "doStartPushSdk: init android id:" + mAndroidId);
        }
        mLoadbalancer = serverLbsUrl;
        log.warn("start push sdk" +
                " , loadbalancer = " + mLoadbalancer +
                " , defaultIp = " + mDefaultIp +
                " , defaultPort = " + mDefaultPort +
                " , appid = " + appid +
                " , manufactorer = " + mManufactor +
                " , imei = " + mImei +
                " , mac = " + mMac +
                " , androidid = " + mAndroidId +
                " , mOfflineTimeoutsec = " + mOfflineTimeoutsec +
                " , mRetryIntervalSec = " + mRetryIntervalSec +
                " , mRetryCount = " + mRetryCount +
                " , mDeadTimeouotSec = " + mDeadTimeouotSec );
        mAppid = appid;
        mAppKey = appKey;
        mPushCallback = pushCallback;
        if (null == mAppKey) {
            mAppKey = "";
        }
        mInited = true;
        libpushclient.native_pushSetDefaultServerAddr(mDefaultIp, mDefaultPort);
        libpushclient.native_pushSetLoadBalancer(mLoadbalancer);
        libpushclient.native_pushSetServerOptions(mOfflineTimeoutsec, mRetryIntervalSec, mRetryCount, mDeadTimeouotSec);
        restartPushSdk();
    }
    /**
     * 发送上行消息
     *
     * @param msgid 消息ID
     * @param ttlSeconds 过期时间
     * @param contentType 消息类型
     * @param content 消息内容
     * @return 0 成功
     *         非0 失败
     */
    @SuppressLint("DefaultLocale")
    public int sendUpStreamMsg(String msgid, long ttlSeconds, String contentType, String content) {
        return sendUpStreamMsg("", msgid, ttlSeconds, contentType, content);
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
     * @param serverLbsUrl 服务器地址
     *        开发: http://iot-api.dev.101.com:1770/v5/sdk/access
     *        测试: http://172.24.132.143:8757/v5/sdk/access
     *        预生产: http://iot-api.beta.101.com:1770/v5/sdk/access
     *        生产: http://iot-api.101.com:1770/v5/sdk/access
     *        香港: http://iot-api.hk.101.com:1770/v5/sdk/access
     *        CA: http://iot-api.awsca.101.com:1770/v5/sdk/access
     *        埃及自用演练：http://172.24.132.63:8757/v5/sdk/access
     *        埃及OMO演练：https://omo-mdm-pushlbs.moe.101.com/v5/sdk/access
     * @param pushCallback 消息到来的回调
     */
    @SuppressLint("DefaultLocale")
    public void startPushSdk(final Context context, final String appid, final String appKey, final String serverLbsUrl, final IPushSdkCallback pushCallback) {
        setupLogConfigurator(context);

        Log.e(TAG,String.format("startPushSdk(appid=%s, appKey=%s, serverLbs=%s)", appid, appKey != null ? appKey : "null", serverLbsUrl));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,String.format("before run startPushSdk(appid=%s, appKey=%s, serverLbs=%s)", appid, appKey != null ? appKey : "null", serverLbsUrl));
                doStartPushSdk(context, appid, appKey, serverLbsUrl, pushCallback);
                Log.e(TAG,String.format("after run startPushSdk(appid=%s, appKey=%s, serverLbs=%s)", appid, appKey != null ? appKey : "null", serverLbsUrl));
            }
        });
    }

    private void setupLogConfigurator(Context pContext){
        try {

            String sdCard = AdhocStorageAdapter.getFilesDir("log");

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
                String logPath = sdCard + "adhoclog/";
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
     * 可选设置，设置心跳时间
     *
     * 比如要求30秒内检测到掉线， 传入  20 3 3 60
     * 注意：所有参数都不为0才有效， 只要其中一个为零即宣告无效（使用服务端默认配置）
     */
    @SuppressLint("DefaultLocale")
    public void setServerOption(final int offlineTimeoutsec, final int retryIntervalSec, final int retryCount, final int deadTimeouotSec) {
        log.info(String.format("setServerOption(offlineTimeoutsec=%d, retryIntervalSec=%d, retryCount=%d, deadTimeouotSec=%d)",
                offlineTimeoutsec, retryIntervalSec, retryCount, deadTimeouotSec));
        mOfflineTimeoutsec = offlineTimeoutsec;
        mRetryIntervalSec = retryIntervalSec;
        mRetryCount = retryCount;
        mDeadTimeouotSec = deadTimeouotSec;
        executorService.submit(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                log.info(String.format("before setServerOption(offlineTimeoutsec=%d, retryIntervalSec=%d, retryCount=%d, deadTimeouotSec=%d)",
                        offlineTimeoutsec, retryIntervalSec, retryCount, deadTimeouotSec));
                libpushclient.native_pushSetServerOptions(offlineTimeoutsec, retryIntervalSec, retryCount, deadTimeouotSec);
                log.info(String.format("after setServerOption(offlineTimeoutsec=%d, retryIntervalSec=%d, retryCount=%d, deadTimeouotSec=%d)",
                        offlineTimeoutsec, retryIntervalSec, retryCount, deadTimeouotSec));
            }
        });
    }

    /**
     * 设置负载均衡服务
     *
     * 开发: http://iot-api.dev.101.com:1770/v5/sdk/access
     * 测试: http://172.24.132.143:8757/v5/sdk/access
     * 预生产: http://iot-api.beta.101.com:1770/v5/sdk/access
     * 生产: http://iot-api.101.com:1770/v5/sdk/access
     * 香港: http://iot-api.hk.101.com:1770/v5/sdk/access
     * CA: http://iot-api.awsca.101.com:1770/v5/sdk/access
     * 埃及自用演练：http://172.24.132.63:8757/v5/sdk/access
     * 埃及OMO演练：https://omo-mdm-pushlbs.moe.101.com/v5/sdk/access
     *
     * @param url 负载均衡服务地址
     */
    @SuppressLint("DefaultLocale")
    public void setLoadBalancer(final String url) {
        log.info(String.format("setLoadBalancer(url=%s)", url));
        mLoadbalancer = url;
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
     * 设置默认服务地址
     */
    @SuppressLint("DefaultLocale")
    public void setDefaultServerAddr(final String ip, final int port) {
        log.info(String.format("setDefaultServerAddr(ip=%s,port=%d)", ip, port));
        mDefaultIp = ip;
        mDefaultPort = port;
        executorService.submit(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                log.info(String.format("before run setDefaultServerAddr(ip=%s,port=%d)", ip, port));
                libpushclient.native_pushSetDefaultServerAddr(ip, port);
                log.info(String.format("after run setDefaultServerAddr(ip=%s,port=%d)", ip, port));
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
        String token = mDevicetoken;
        if (token == null) {
            log.info("setAlias error:token not set");
            return -1;
        }
        if (alias != null && alias.equals(mAlias)) {
            //同样的token已经绑定过这个别名
            log.info("setAlias : alias already set :" + alias);
            return 0;
        }
        int result = libpushclient.native_pushSetAlias(alias);
        log.info("setAlias result:" + result);
        if (result == 0) {
            //绑定成功了，保存别名
            mAlias = alias;
        }
        return result;
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
                " , Loadbalancer = " + mLoadbalancer +
                " , appid = " + mAppid +
                " , manufactorer = " + mManufactor +
                " , imei = " + mImei +
                " , mac = " + mMac +
                " , androidid = " + mAndroidId);
        doNotifyClientConnectStatus(false);
        boolean needstart = false;
        if (mLoadbalancer == null) {
            Log.e(TAG,"Loadbalancer is null");
        }
        if (mAppid == null || mAppid.isEmpty()) {
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
                Log.d(TAG, "native_pushLogin start "
                        + " loadbalancer: " + mLoadbalancer
                        + " defaultIp: " + mDefaultIp
                        + " defaultPort: " + mDefaultPort
                        + " appID:" + mAppid
                        + " appKey:" + mAppKey
                        + " manufactor:" + mManufactor
                        + " imei:" + mImei + " mac:" + mMac);
                libpushclient.native_pushSetDefaultServerAddr(mDefaultIp, mDefaultPort);
                libpushclient.native_pushSetLoadBalancer(mLoadbalancer);
                libpushclient.native_pushSetServerOptions(mOfflineTimeoutsec, mRetryIntervalSec, mRetryCount, mDeadTimeouotSec);
                libpushclient.native_pushLogin(mAppid, mAppKey, mManufactor, mImei,
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
                if (deviceToken != null && !deviceToken.equals(mDevicetoken)) {
                    //传进来新的token,把缓存的别名清掉
                    mAlias = null;
                }
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
    public void notifyPushMessage(final String appId, final int msgtype, final byte[] contenttype, final long msgid, final long msgTime, final String topic, final byte[] data, final String[] extraKeys, final String[] extraValues) {
        Log.e(TAG,String.format("notifyPushMessage(appid=%s, msgtype=%d, msgid=%d, msgtime=%d, topic=%s)", appId, msgtype, msgid, msgTime, topic));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,String.format("before run notifyPushMessage(appid=%s, msgtype=%d, msgid=%d, msgtime=%d, topic=%s)", appId, msgtype, msgid, msgTime, topic));
                if (mPushCallback != null) {
                    try {
                        mPushCallback.onPushMessage(mAppid, msgtype, contenttype, msgid, msgTime, topic, data, extraKeys, extraValues);
                    } catch (Exception e) {
                        Log.e(TAG, "process push message error:"+e.toString());
                    }
                }
                Log.e(TAG,String.format("after run notifyPushMessage(appid=%s, msgtype=%d, msgid=%d, msgtime=%d, topic=%s)", appId, msgtype, msgid, msgTime, topic));
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
