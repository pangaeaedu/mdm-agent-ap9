package com.nd.adhoc.push;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.nd.adhoc.push.service.PushService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushSdk {
    private static PushSdk instance = new PushSdk();

    private static Logger log = LoggerFactory.getLogger(PushSdk.class.getSimpleName());

    private Context mContext;

    private String mIp;

    private int mPort;

    private String mAppid;

    private PushSdkCallback mPushCallback;

    /**
     * ServiceConnection代表与服务的连接，它只有两个方法，
     * onServiceConnected和onServiceDisconnected，
     * 前者是在操作者在连接一个服务成功时被调用，而后者是在服务崩溃或被杀死导致的连接中断时被调用
     */
    private ServiceConnection mServiceConnection;
    private PushService mPushService;

    public static PushSdk getInstance() {
        log.info("getInstance()");
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
    public synchronized void startPushSdk(final Context context, String appid, String ip, int port, PushSdkCallback pushCallback) {
        log.info("startPushSdk()");
        mServiceConnection = new ServiceConnection() {
            /**
             * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
             * 通过这个IBinder对象，实现宿主和Service的交互。
             */
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                log.info("onServiceConnected()");
                // 获取Binder
                PushService.PushServiceBinder binder = (PushService.PushServiceBinder) service;
                mPushService = binder.getService();
                if (mPushService != null) {
                    mPushService.startPushSdk(mContext, mAppid, mIp, mPort, mPushCallback);
                }
            }

            /**
             * 当取消绑定的时候被回调。但正常情况下是不被调用的，它的调用时机是当Service服务被意外销毁时，
             * 例如内存的资源不足时这个方法才被自动调用。
             */
            @Override
            public void onServiceDisconnected(ComponentName name) {
                log.info("onServiceDisconnected()");
                mPushService = null;
            }
        };
        mContext = context.getApplicationContext();
        mIp = ip;
        mPort = port;
        mAppid = appid;
        mPushCallback = pushCallback;

        if (mPushService == null) {
            Intent intent = new Intent(mContext, PushService.class);
            ComponentName componentName = mContext.startService(intent);
            // 将flags设成0x0000，是因为设成其他时，当Service无人Bind时，会自动关闭
            boolean ret = mContext.bindService(intent, mServiceConnection, 0x0000);
        }
    }

    /**
     * 断开并重新连接push服务
     */
    public synchronized void restartPushSdk() {
        log.info("restartPushSdk()");
        if (mPushService != null) {
            mPushService.restartPushSdk();
        }
    }

    /**
     * 停止接收push消息
     */
    public synchronized void stop() {
        log.info("stop()");
        if (mPushService != null) {
            mPushService.stop();
        }
        if (mContext != null) {
            if (mServiceConnection != null) {
                mContext.unbindService(mServiceConnection);
            }
            Intent intent = new Intent(mContext, PushService.class);
            mContext.startService(intent);
        }
        mPushService = null;
    }

    /**
     * @return 返回是否与push服务连接着
     */
    public synchronized boolean isConnected() {
        log.info("isConnected()");
        if (mPushService != null) {
            return mPushService.isConnected();
        } else {
            return false;
        }
    }

    /**
     * @return 返回deviceId
     */
    public synchronized String getDeviceid() {
        log.info("getDeviceid()");
        if (mPushService != null) {
            return mPushService.getDeviceid();
        } else {
            return null;
        }
    }

    public synchronized void notifyClientConnectStatus(boolean isConnected) {
        log.info("notifyClientConnectStatus()");
        if (mPushService != null) {
            mPushService.notifyClientConnectStatus(isConnected);
        }
    }

    public synchronized void notifyDeviceToken(String deviceToken) {
        log.info("notifyDeviceToken()");
        if (mPushService != null) {
            mPushService.notifyDeviceToken(deviceToken);
        }
    }

    public synchronized void notifyPushMessage(long msgId, long msgTime, byte[] data) {
        log.info("notifyPushMessage()");
        if (mPushService != null) {
            mPushService.notifyPushMessage(msgId, msgTime, data);
        }
    }

}
