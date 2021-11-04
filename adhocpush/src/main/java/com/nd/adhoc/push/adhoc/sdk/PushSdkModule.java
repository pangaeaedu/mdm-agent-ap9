package com.nd.adhoc.push.adhoc.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.nd.android.adhoc.basic.util.storage.AdhocStorageAdapter;

import com.nd.adhoc.push.client.libpushclient;
import com.nd.adhoc.push.adhoc.utils.DeviceUtil;
import com.nd.sdp.adhoc.push.IPushSdkCallback;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

    private String mCacheDir = "";

    private int mReconnectIntervalMs = 10000;

    // 默认用 20，3，3，60
    private int mOfflineTimeoutsec = 20, mRetryIntervalSec = 3, mRetryCount = 3, mDeadTimeouotSec = 60;

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

    private Context mContext = null;

    public static PushSdkModule getInstance() {
        return instance;
    }

    /**
     * 开始接收Push消息
     *
     * @param context      context
     * @param appid        从Push后台申请的appId
     * @param serverLbsUrl 服务器地址
     *                     开发: http://iot-api.dev.101.com:1770/v5/sdk/access
     *                     测试: http://172.24.132.143:8757/v5/sdk/access
     *                     预生产: http://iot-api.beta.101.com:1770/v5/sdk/access
     *                     生产: http://iot-api.101.com:1770/v5/sdk/access
     *                     香港: http://iot-api.hk.101.com:1770/v5/sdk/access
     *                     CA: http://iot-api.awsca.101.com:1770/v5/sdk/access
     *                     埃及自用演练：http://172.24.132.63:8757/v5/sdk/access
     *                     埃及OMO演练：https://omo-mdm-pushlbs.moe.101.com/v5/sdk/access
     * @param pushCallback 消息到来的回调
     */
    private void doStartPushSdk(final Context context, String appid, String appKey, String serverLbsUrl, IPushSdkCallback pushCallback) {
        final String packageName = context.getPackageName();
        if (!mInited) {
            Log.i(TAG, "doStartPushSdk: init data");
            if (mCacheDir.length() == 0) {
                String sdCard = AdhocStorageAdapter.getFilesDir("log");
                if (null != sdCard) {
                    String logPath = sdCard + "adhoclog/";
                    mCacheDir = logPath;
                }
            }

            libpushclient.native_pushInit(mCacheDir);


//            String pseudoId = DeviceUtil.getPseudoId();
            String deviceUUID = DeviceUtil.getDeviceUUID(context, false);
            ;
            mManufactor = DeviceUtil.getManufactorer();
            mImei = deviceUUID;//DeviceUtil.getImei(context);
//            if (null == mImei || mImei.isEmpty()) {
//                mImei = deviceUUID;
//            }
            mMac = getCachedUUID();//DeviceUtil.getMac(context);
            mAndroidId = DeviceUtil.getAndroidId(context);

            if (mMac.length() > 0) {
                mImei = "";
                mAndroidId = "";
                log.warn("android use mac only");
            }
        }
        mLoadbalancer = serverLbsUrl;
        log.warn("start push sdk" +
                " , param1 = " + mLoadbalancer +
                " , param2 = " + mDefaultIp +
                " , param3 = " + mDefaultPort +
                " , param4 = " + appid +
                " , manufactorer = " + mManufactor +
                " , param5 = " + mImei +
                " , param6 = " + mMac +
                " , androidid = " + mAndroidId +
                " , mOfflineTimeoutsec = " + mOfflineTimeoutsec +
                " , mRetryIntervalSec = " + mRetryIntervalSec +
                " , mRetryCount = " + mRetryCount +
                " , mDeadTimeouotSec = " + mDeadTimeouotSec);
        mAppid = appid;
        mAppKey = appKey;
        mPushCallback = pushCallback;
        if (null == mAppKey) {
            mAppKey = "";
        }
        mInited = true;
        libpushclient.native_pushSetDefaultServerAddr(mDefaultIp, mDefaultPort);
        if (mLoadbalancer != null) {
            libpushclient.native_pushSetLoadBalancer(mLoadbalancer);
        }
        libpushclient.native_pushSetServerOptions(mOfflineTimeoutsec, mRetryIntervalSec, mRetryCount, mDeadTimeouotSec);
        doConnectPush();
    }

    private String getCachedUUIDPath() {
        if (mCacheDir.length() > 0 && mCacheDir.charAt(mCacheDir.length() - 1) == '/') {
            return mCacheDir + "uuid.uuid";
        }
        return mCacheDir + "/uuid.uuid";
    }

    private String getCachedUUID() {
        String uuidPath = getCachedUUIDPath();
        try {
            FileInputStream fis = new FileInputStream(uuidPath);
            byte[] lsy = new byte[fis.available()];
            int read = fis.read(lsy);
            fis.close();
            String jsonStr = new String(lsy, 0, read);
            JSONObject obj = new JSONObject(jsonStr);
            String uuid = obj.getString("uuid");
            if (uuid.length() != 0) {
                log.warn("get sd cached uuid success , path = " + uuidPath + ", value = " + uuid);

                // write to shared reference
                writeUUIDToSP(uuid);

                return uuid;
            }
        } catch (Exception e) {
            log.warn("get sd cached uuid failed , path = " + uuidPath + ", e = " + e.toString());
        }
        return getSPCachedUUID();
    }

    private String getSPCachedUUID() {
        try {
            SharedPreferences sp = mContext.getSharedPreferences("XPUSHSP", Context.MODE_PRIVATE);
            String uuid = sp.getString("uuid", "");
            if (uuid.length() > 0) {
                log.warn("get sp cached uuid success , value = " + uuid);
                return uuid;
            } else {
                log.warn("get sp cached uuid empty ");
                return createCachedUUID();
            }
        } catch (Exception e) {
            log.warn("get sp cached uuid failed , " + "e = " + e.toString());
        }
        return createCachedUUID();
    }

    private void writeUUIDToSP(String uuid) {
        try {
            SharedPreferences sp = mContext.getSharedPreferences("XPUSHSP", Context.MODE_PRIVATE);
            sp.edit().putString("uuid", uuid).apply();
            log.warn("set sp cached uuid success , value = " + uuid);
        } catch (Exception e) {
            log.warn("set sp cached uuid failed , value = " + uuid + ", e = " + e.toString());
        }

    }

    private String createCachedUUID() {
        String uuid = UUID.randomUUID().toString();
        writeUUIDToSD(uuid);
        writeUUIDToSP(uuid);
        return uuid;
    }

    private void writeUUIDToSD(String uuid) {
        String uuidPath = getCachedUUIDPath();
        File myFilePath = new File(uuidPath);
        try {
            if (!myFilePath.exists()) {
                myFilePath.createNewFile();
            } else {
                myFilePath.delete();
                myFilePath.createNewFile();
            }
            FileWriter resultFile = new FileWriter(myFilePath);
            PrintWriter myFile = new PrintWriter(resultFile);
            JSONObject obj = new JSONObject();
            obj.put("uuid", uuid);
            myFile.println(obj.toString());
            resultFile.close();
            log.warn("set sd cached uuid success , key = " + uuidPath + ", value = " + obj.toString());
        } catch (IOException | JSONException e) {
            log.warn("set sd cached uuid failed , key = " + uuidPath + ",e = " + e.toString());
        }
    }


    /**
     * 发送上行消息
     *
     * @param msgid       消息ID
     * @param ttlSeconds  过期时间
     * @param contentType 消息类型
     * @param content     消息内容
     * @return 0 成功
     * 非0 失败
     */
    @SuppressLint("DefaultLocale")
    public int sendUpStreamMsg(String msgid, long ttlSeconds, String contentType, String content) {
        return sendUpStreamMsg("", msgid, ttlSeconds, contentType, content);
    }

    /**
     * 发送上行消息
     *
     * @param topic       主题 ，默认为空
     * @param msgid       消息ID
     * @param ttlSeconds  过期时间
     * @param contentType 消息类型
     * @param content     消息内容
     * @return 0 成功
     * 非0 失败
     */
    @SuppressLint("DefaultLocale")
    public int sendUpStreamMsg(String topic, String msgid, long ttlSeconds, String contentType, String content) {
        String extraHeaders = "{\"$IoT/mq_topic\":\"" + topic + "\"}";
        int ret = libpushclient.native_sendUpStreamMsg(msgid, ttlSeconds, contentType, content, extraHeaders);

        if (ret != 0) {
            Log.e(TAG, "sendUpStreamMsg failed topic:" + topic + "msgid:" + msgid + " ret:" + ret);
            notifyPushUpstreamSent(msgid, ret);
        }

        return ret;
    }

    /**
     * 发送主题消息
     *
     * @param topic   主题 ，默认为空
     * @param qos     见 PushQoS
     * @param content 消息内容
     * @return 0 成功
     * 非0 失败
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
     *                     开发: http://iot-api.dev.101.com:1770/v5/sdk/access
     *                     测试: http://172.24.132.143:8757/v5/sdk/access
     *                     预生产: http://iot-api.beta.101.com:1770/v5/sdk/access
     *                     生产: http://iot-api.101.com:1770/v5/sdk/access
     *                     香港: http://iot-api.hk.101.com:1770/v5/sdk/access
     *                     CA: http://iot-api.awsca.101.com:1770/v5/sdk/access
     *                     埃及自用演练：http://172.24.132.63:8757/v5/sdk/access
     *                     埃及OMO演练：https://omo-mdm-pushlbs.moe.101.com/v5/sdk/access
     * @param pushCallback 消息到来的回调
     */
    @SuppressLint("DefaultLocale")
    public void startPushSdk(final Context context, final String appid, final String appKey, final String serverLbsUrl, final IPushSdkCallback pushCallback) {
        mContext = context;
        setupLogConfigurator(context);

        Log.e(TAG, String.format("startPushSdk(param=%s, param=%s, param=%s)", appid, appKey != null ? appKey : "null", serverLbsUrl));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, String.format("before run startPushSdk(param=%s, param=%s, param=%s)", appid, appKey != null ? appKey : "null", serverLbsUrl));
                doStartPushSdk(context, appid, appKey, serverLbsUrl, pushCallback);
                Log.e(TAG, String.format("after run startPushSdk(param=%s, param=%s, param=%s)", appid, appKey != null ? appKey : "null", serverLbsUrl));
            }
        });
    }

    private void setupLogConfigurator(Context context) {
        final String packageName = context.getPackageName();
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
        } catch (Exception e) {
            configureLogcat();
        }
    }

    private void configureLogcat() {
        try {
            LogConfigurator logConfigurator = new LogConfigurator();
            logConfigurator.setRootLevel(Level.ALL);
            logConfigurator.setUseFileAppender(false);
            logConfigurator.setUseLogCatAppender(true);
            logConfigurator.configure();
            log.warn("no permission for log , use logcat");
        } catch (Exception e) {

        }
    }

    /**
     * 设置厂商推送通道（在PushLogin之前调用）
     *
     * @param packageName     在这个厂商的包名
     * @param manufactorName  厂商名
     * @param manufactorToken 厂商分配的设备Token
     */
    public synchronized void setMFChannel(final String packageName, final String manufactorName, final String manufactorToken) {
        log.info(String.format("setMFChannel(packageName=%s, manufactorName=%s, param1=%s)", packageName, manufactorName, manufactorToken));
        this.packageName = packageName;
        this.manufactorName = manufactorName;
        this.manufactorToken = manufactorToken;
        executorService.submit(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                log.info(String.format("before run setMFChannel(packageName=%s, manufactorName=%s, param1=%s)", packageName, manufactorName, manufactorToken));
                libpushclient.native_setMFChannel(packageName, manufactorName, manufactorToken);
                log.info(String.format("after run setMFChannel(packageName=%s, manufactorName=%s, param1=%s)", packageName, manufactorName, manufactorToken));
            }
        });
    }

    /**
     * 可选设置，设置心跳时间
     * <p>
     * 比如要求30秒内检测到掉线， 传入  20 3 3 60
     * 注意：所有参数都不为0才有效， 只要其中一个为零即宣告无效（使用服务端默认配置）
     */
    @SuppressLint("DefaultLocale")
    public void setServerOption(final int offlineTimeoutsec, final int retryIntervalSec, final int retryCount, final int deadTimeouotSec) {
        log.info(String.format("setServerOption(offlineTimeoutsec=%d, retryIntervalSec=%d, retryCount=%d, deadTimeouotSec=%d)",
                offlineTimeoutsec, retryIntervalSec, retryCount, deadTimeouotSec));
        if (offlineTimeoutsec == 0 && retryIntervalSec == 0 && retryCount == 0 && deadTimeouotSec == 0) {
            useDefServerOption();
        } else {
            mOfflineTimeoutsec = offlineTimeoutsec;
            mRetryIntervalSec = retryIntervalSec;
            mRetryCount = retryCount;
            mDeadTimeouotSec = deadTimeouotSec;
        }

        executorService.submit(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                log.info(String.format("setServerOption(offlineTimeoutsec=%d, retryIntervalSec=%d, retryCount=%d, deadTimeouotSec=%d)",
                        offlineTimeoutsec, retryIntervalSec, retryCount, deadTimeouotSec));
                libpushclient.native_pushSetServerOptions(offlineTimeoutsec, retryIntervalSec, retryCount, deadTimeouotSec);
                //要重新login，否则服务端不会去变更心跳频率
                //libpushclient.native_pushDisconnect();
                //libpushclient.native_pushLogin(mAppid, mAppKey, mManufactor, mImei, mMac, mAndroidId, mReconnectIntervalMs);
                //log.info("restart native_pushLogin");
            }
        });
    }

    private void useDefServerOption() {
        mOfflineTimeoutsec = 20;
        mRetryIntervalSec = 3;
        mRetryCount = 3;
        mDeadTimeouotSec = 60;
    }

    /**
     * 设置负载均衡服务
     * <p>
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
        log.info(String.format("setParamBalance(param=%s)", url));
        if (url == null) {
            return;
        }
        mLoadbalancer = url;
        executorService.submit(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                log.info(String.format("before run setLoadBalancer"));
                libpushclient.native_pushSetLoadBalancer(url);
                log.info(String.format("after run setLoadBalancer"));
            }
        });
    }

    /**
     * 设置默认服务地址
     */
    @SuppressLint("DefaultLocale")
    public void setDefaultServerAddr(final String ip, final int port) {
        log.info(String.format("setParam1(param1=%s,param2=%d)", ip, port));
        mDefaultIp = ip;
        mDefaultPort = port;
        executorService.submit(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                log.info(String.format("before run setparam1(param1=%s,param2=%d)", ip, port));
                libpushclient.native_pushSetDefaultServerAddr(ip, port);
                log.info(String.format("after run setparam1(param1=%s,param2=%d)", ip, port));
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
        for (Map.Entry<String, PushQoS> topic : topics.entrySet()) {
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
    public void addTags(Map<String, String> tags) {
        String[] keys = new String[tags.size()];
        String[] values = new String[tags.size()];
        int index = 0;
        for (Map.Entry<String, String> tag : tags.entrySet()) {
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
    public void removeTags(String[] keys) {
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
     * @return 0               表示成功
     * 非零值           错误码, 即使返回错误码， 也有可能成功， 因为等待服务端回应只等待1秒， 服务端仍然有可能成功
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
    public void triggetReconnect() {
        executorService.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        initPushParams();
                    }
                });

    }


    public void setAutoStart(boolean pStart) {
        Log.e(TAG, "setAutoStart:" + pStart);
        mAutoStart = pStart;
    }

    private void doConnectPush() {
        Log.e(TAG, "before run restartPushSdk");
        mIsScheduleStarting = false;
        mLastRestartTimestampMs = SystemClock.elapsedRealtime();
        Log.e(TAG, "restart push sdk" +
                " , param1 = " + mLoadbalancer +
                " , param2 = " + mAppid +
                " , manufactorer = " + mManufactor +
                " , param3 = " + mImei +
                " , param4 = " + mMac +
                " , androidid = " + mAndroidId);
        doNotifyClientConnectStatus(false);
        boolean needstart = false;
        if (mLoadbalancer == null) {
            Log.e(TAG, "Loadbalancer is null");
        }
        if (mAppid == null || mAppid.isEmpty()) {
            Log.e(TAG, "App id is null");
        } else {
            if (mManufactor == null || mManufactor.isEmpty()) {
                Log.e(TAG, "Manufactor is null");
                mManufactor = "";
            }
            if (mImei == null || mImei.isEmpty()) {
                Log.e(TAG, "Imei is null");
                mImei = "";
            } else {
                needstart = true;
            }
            if (mMac == null || mMac.isEmpty()) {
                Log.e(TAG, "Mac is null");
                mMac = "";
            } else {
                needstart = true;
            }
            if (mAndroidId == null || mAndroidId.isEmpty()) {
                Log.e(TAG, "AndroidId is null");
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
                        + " param1: " + mLoadbalancer
                        + " param2: " + mDefaultIp
                        + " param3: " + mDefaultPort
                        + " param4:" + mAppid
                        + " param5:" + mAppKey
                        + " manufactor:" + mManufactor
                        + " param6:" + mImei
                        + " androidid:" + mAndroidId
                        + " param7:" + mMac);
                initPushParams();
                libpushclient.native_pushLogin(mAppid, mAppKey, mManufactor, mImei, mMac, mAndroidId, mReconnectIntervalMs);
                Log.e(TAG, "after run doConnectPush");
            } else {
                Log.d(TAG, "auto start is false, not calling native_pushLogin");
            }
        } else {
            log.error("Can't doConnectPush, device info is empty");
        }
    }

    private void initPushParams() {
        log.info("initPushParams  "
                + " param1: " + mLoadbalancer
                + " param2: " + mDefaultIp
                + " param4: " + mDefaultPort
                + " packageName: " + packageName
                + " manufactorName: " + manufactorName
                + " param4: " + manufactorToken
                + " mOfflineTimeoutsec: " + mOfflineTimeoutsec
                + " mRetryIntervalSec: " + mRetryIntervalSec
                + " mRetryCount: " + mRetryCount
                + " mDeadTimeouotSec: " + mDeadTimeouotSec);
        libpushclient.native_setMFChannel(packageName, manufactorName, manufactorToken);
        libpushclient.native_pushSetDefaultServerAddr(mDefaultIp, mDefaultPort);
        if (mLoadbalancer != null) {
            libpushclient.native_pushSetLoadBalancer(mLoadbalancer);
        }
        libpushclient.native_pushSetServerOptions(mOfflineTimeoutsec, mRetryIntervalSec, mRetryCount, mDeadTimeouotSec);
    }

    /**
     * 停止接收push消息
     */
    public void stop() {
        Log.e(TAG, "stop()");
        mIsConnected = false;
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "before run stop()");
                libpushclient.native_pushDisconnect();
                Log.e(TAG, "after run stop()");
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

    public void notifyPushUpstreamSent(final String msgid, final int errCode) {
        log.info(String.format("notifyPushUpstreamSent(%s, %d)", msgid, errCode));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                log.info(String.format("before run notifyPushUpstreamSent(%s, %d)", msgid, errCode));
                if (mPushCallback != null) {
                    try {
                        mPushCallback.onPushUpstreamSent(msgid, errCode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                log.info(String.format("after run notifyPushUpstreamSent(%s, %d)", msgid, errCode));
            }
        });
    }

    public void notifyClientConnectStatus(final boolean isConnected) {
        Log.e(TAG, String.format("notifyClientConnectStatus(%b)", isConnected));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, String.format("before run notifyClientConnectStatus(%b)", isConnected));
                doNotifyClientConnectStatus(isConnected);
                Log.e(TAG, String.format("after run notifyClientConnectStatus(%b)", isConnected));
            }
        });

    }

    private void doNotifyClientConnectStatus(final boolean isConnected) {
        Log.e(TAG, String.format("doNotifyClientConnectStatus(%b)", isConnected));

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, String.format("before run doNotifyClientConnectStatus(%b)", isConnected));

                Log.e(TAG, "doNotifyClientConnectStatus" +
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

                Log.e(TAG, String.format("after run doNotifyClientConnectStatus(%b)", isConnected));
            }
        });

    }



    public void notifyClientLoginResult(final int pCode) {
        Log.e(TAG, "notifyClientLoginResult: " + pCode);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "before run notifyClientLoginResult: " + pCode);
                doNotifyClientLoginResult(pCode);
                Log.e(TAG, "after run notifyClientLoginResult: " + pCode);
            }
        });

    }

    private void doNotifyClientLoginResult(final int pCode) {
        Log.e(TAG, "doNotifyClientLoginResult: " + pCode);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "before run doNotifyClientLoginResult: " + pCode);

                if (mPushCallback != null) {
                    try {
                        mPushCallback.onPushLoginResult(pCode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public void notifyDeviceToken(final String deviceToken) {
        Log.e(TAG, "notify(value = " + deviceToken + ")");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "before run notify");
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
                Log.e(TAG, "after run notify");
            }
        });
    }

    @SuppressLint("DefaultLocale")
    public void notifyPushMessage(final String appId, final int msgtype, final byte[] contenttype, final long msgid, final long msgTime, final String topic, final byte[] data, final String[] extraKeys, final String[] extraValues) {
        Log.e(TAG, String.format("notifyPushMessage(param1=%s, msgtype=%d, msgid=%d, msgtime=%d, topic=%s)", appId, msgtype, msgid, msgTime, topic));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, String.format("before run notifyPushMessage(param1=%s, msgtype=%d, msgid=%d, msgtime=%d, topic=%s)", appId, msgtype, msgid, msgTime, topic));
                if (mPushCallback != null) {
                    try {
                        mPushCallback.onPushMessage(mAppid, msgtype, contenttype, msgid, msgTime, topic, data, extraKeys, extraValues);
                    } catch (Exception e) {
                        Log.e(TAG, "process push message error:" + e.toString());
                    }
                }
                Log.e(TAG, String.format("after run notifyPushMessage(param1=%s, msgtype=%d, msgid=%d, msgtime=%d, topic=%s)", appId, msgtype, msgid, msgTime, topic));
            }
        });
    }

    public void notifyPushShadowUpdated(final PushShadowMode mode, final String shadow) {
        log.info(String.format("notifyPushShadowUpdated(mode=%s,shadow=%s)", mode.getStringValue(), shadow));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                log.info("before run notifyPushShadowUpdated");
                if (mPushCallback != null) {
                    try {
                        mPushCallback.onPushShadowUpdated(mode.getIntValue(), shadow);
                    } catch (Exception e) {
                        log.info(e.toString());
                    }
                }
                log.info("after run notifyPushShadowUpdated");
            }
        });
    }

    public void init(String cacheDir) {
        log.info(String.format("init(cacheDir=%s)", cacheDir));
        mCacheDir = cacheDir;
    }
}
