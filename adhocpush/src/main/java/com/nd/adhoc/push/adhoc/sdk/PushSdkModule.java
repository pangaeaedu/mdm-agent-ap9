package com.nd.adhoc.push.adhoc.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import com.nd.adhoc.push.adhoc.utils.DeviceUtil;
import com.nd.adhoc.push.adhoc.utils.StorageUtil;
import com.nd.adhoc.push.client.libpushclient;
import com.nd.sdp.adhoc.push.IPushSdkCallback;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.mindpipe.android.logging.log4j.LogConfigurator;


public class PushSdkModule {
    private static Logger log = Logger.getLogger(PushSdkModule.class.getSimpleName());
    private static PushSdkModule instance = new PushSdkModule();

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
            final String packageName = context.getPackageName();
            File sdCard = Environment.getExternalStorageDirectory();
            if (null == sdCard) {
                sdCard = Environment.getDownloadCacheDirectory();
            }
            if (null != sdCard) {
                String logPath = sdCard + "/" + packageName + "/adhoclog/";
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
        log.warn("start push sdk" +
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
     * @param msgid 消息ID
     * @param ttlSeconds 过期时间
     * @param contentType 消息类型
     * @param content 消息内容
     * @return 0 成功
     *         非0 失败
     */
    @SuppressLint("DefaultLocale")
    public int sendUpStreamMsg(String msgid, long ttlSeconds, String contentType, String content) {
        return libpushclient.native_sendUpStreamMsg(msgid, ttlSeconds, contentType, content);
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
        final String packageName = context.getPackageName();
        String sdCardPath = StorageUtil.getSdCardPath();
        if (null == sdCardPath || sdCardPath.isEmpty()) {
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
            String logPath = sdCardPath + "/" + packageName + "/adhoclog/";
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
        log.info(String.format("startPushSdk(appid=%s, appKey=%s, ip=%s, port=%d)", appid, appKey != null ? appKey : "null", ip, port));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                log.info(String.format("before run startPushSdk(appid=%s, appKey=%s, ip=%s, port=%d)", appid, appKey != null ? appKey : "null", ip, port));
                doStartPushSdk(context, appid, appKey, ip, port, pushCallback);
                log.info(String.format("after run startPushSdk(appid=%s, appKey=%s, ip=%s, port=%d)", appid, appKey != null ? appKey : "null", ip, port));
            }
        });
    }

    /**
     * 设置负载均衡服务
     *
     * @param host 负载均衡服务地址
     * @param port 负载均衡服务端口
     */
    @SuppressLint("DefaultLocale")
    public void setLoadBalancer(final String host, final int port) {
        log.info(String.format("setLoadBalancer(host=%s, port=%d)", host, port));
        executorService.submit(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                log.info(String.format("before run setLoadBalancer(host=%s, port=%d)", host, port));
                libpushclient.native_pushSetLoadBalancer(host, port);
                log.info(String.format("after run setLoadBalancer(host=%s, port=%d)", host, port));
            }
        });
    }

    /**
     * 断开并重新连接push服务
     */
    public void restartPushSdk() {
        log.info("restartPushSdk , version = 0.3.23");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                long lastRestartInterval = System.currentTimeMillis()-mLastRestartTimestampMs;
                long scheduleInterval = RESTART_INTERVAL_MS-lastRestartInterval;
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

    private void doRestartPushSdk() {
        log.info("before run restartPushSdk");
        mIsScheduleStarting = false;
        mLastRestartTimestampMs = System.currentTimeMillis();
        log.warn("restart push sdk" +
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
            log.warn("Ip is null");
        } else if (mPort <= 0) {
            log.warn("Port is wrong. Port = " + mPort);
        } else if (mAppid == null || mAppid.isEmpty()) {
            log.warn("App id is null");
        } else {
            if (mManufactor == null || mManufactor.isEmpty()) {
                log.warn("Manufactor is null");
                mManufactor = "";
            }
            if (mImei == null || mImei.isEmpty()) {
                log.warn("Imei is null");
                mImei = "";
            } else {
                needstart = true;
            }
            if (mMac == null || mMac.isEmpty()) {
                log.warn("Mac is null");
                mMac = "";
            } else {
                needstart = true;
            }
            if (mAndroidId == null || mAndroidId.isEmpty()) {
                log.warn("AndroidId is null");
                mAndroidId = "";
            } else {
                needstart = true;
            }
        }
        if (null == mAppKey) {
            mAppKey = "";
        }
        if (needstart) {
            libpushclient.native_pushLogin(mIp, mPort, mAppid, mAppKey, mManufactor, mImei, mMac, mAndroidId, mReconnectIntervalMs);
            log.info("after run restartPushSdk");
        } else {
            log.info("retry restartPushSdk");
            restartPushSdk();
        }
    }

    /**
     * 停止接收push消息
     */
    public void stop() {
        log.info("stop()");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                log.info("before run stop()");
                libpushclient.native_pushDisconnect();
                log.info("after run stop()");
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
        log.info(String.format("notifyClientConnectStatus(%b)", isConnected));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                log.info(String.format("before run notifyClientConnectStatus(%b)", isConnected));
                doNotifyClientConnectStatus(isConnected);
                log.info(String.format("after run notifyClientConnectStatus(%b)", isConnected));
            }
        });

    }

    private void doNotifyClientConnectStatus(final boolean isConnected) {
        log.info(String.format("doNotifyClientConnectStatus(%b)", isConnected));

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                log.info(String.format("before run doNotifyClientConnectStatus(%b)", isConnected));

                log.warn("doNotifyClientConnectStatus" +
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

                log.info(String.format("after run doNotifyClientConnectStatus(%b)", isConnected));
            }
        });

    }

    public void notifyDeviceToken(final String deviceToken) {
        log.info("notifyDeviceToken(deviceToken = " + deviceToken + ")");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                log.info("before run notifyDeviceToken(deviceToken = " + deviceToken + ")");
                mDevicetoken = deviceToken;
                if (mPushCallback != null) {
                    try {
                        mPushCallback.onPushDeviceToken(mDevicetoken);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                log.info("after run notifyDeviceToken(deviceToken = " + deviceToken + ")");
            }
        });
    }

    @SuppressLint("DefaultLocale")
    public void notifyPushMessage(final String appId, final int msgtype, final byte[] contenttype, final long msgid, final long msgTime, final byte[] data, final String[] extraKeys, final String[] extraValues) {
        log.info(String.format("notifyPushMessage(appid=%s, msgtype=%d, msgid=%d, msgtime=%d)", appId, msgtype, msgid, msgTime));
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                log.info(String.format("before run notifyPushMessage(appid=%s, msgtype=%d, msgid=%d, msgtime=%d)", appId, msgtype, msgid, msgTime));
                if (mPushCallback != null) {
                    try {
                        mPushCallback.onPushMessage(mAppid, msgtype, contenttype, msgid, msgTime, data, extraKeys, extraValues);
                    } catch (Exception e) {
                        log.info(e.toString());
                    }
                }
                log.info(String.format("after run notifyPushMessage(appid=%s, msgtype=%d, msgid=%d, msgtime=%d)", appId, msgtype, msgid, msgTime));
            }
        });
    }

}
