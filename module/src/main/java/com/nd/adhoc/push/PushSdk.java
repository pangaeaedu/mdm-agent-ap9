package com.nd.adhoc.push;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.nd.adhoc.push.service.PushService;
import com.nd.sdp.adhoc.push.IPushSdkCallback;
import com.nd.sdp.adhoc.push.IPushService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushSdk {
    private static PushSdk instance = new PushSdk();

    private static Logger log = LoggerFactory.getLogger(PushSdk.class.getSimpleName());

    private Context mContext;

    private String mIp;

    private int mPort;

    private String mAppid;

    private IPushSdkCallback mPushCallback;

    /**
     * ServiceConnection代表与服务的连接，它只有两个方法，
     * onServiceConnected和onServiceDisconnected，
     * 前者是在操作者在连接一个服务成功时被调用，而后者是在服务崩溃或被杀死导致的连接中断时被调用
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /**
         * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
         * 通过这个IBinder对象，实现宿主和Service的交互。
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            log.info("PushSdk mServiceConnection onServiceConnected()");
            // 获取Binder
            mPushService = IPushService.Stub.asInterface(binder);
            if (mPushService != null) {
                try {
                    mPushService.startPushSdk(mAppid, mIp, mPort, mPushCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 当取消绑定的时候被回调。但正常情况下是不被调用的，它的调用时机是当Service服务被意外销毁时，
         * 例如内存的资源不足时这个方法才被自动调用。
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            log.info("PushSdk mServiceConnection onServiceDisconnected()");
            mPushService = null;
            startPushService(mContext);
        }
    };

    private IPushService mPushService;

    public static PushSdk getInstance() {
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
    public synchronized void startPushSdk(final Context context, String appid, String ip, int port, IPushSdkCallback pushCallback) {
        log.info("startPushSdk()");
        mContext = context.getApplicationContext();
        mIp = ip;
        mPort = port;
        mAppid = appid;
        mPushCallback = pushCallback;

        if (mPushService == null) {
            startPushService(mContext);
        } else {
            try {
                mPushService.startPushSdk(mAppid, mIp, mPort, mPushCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 断开并重新连接push服务
     */
    public synchronized void restartPushSdk() {
        log.info("restartPushSdk()");
        if (mPushService != null) {
            try {
                mPushService.restartPushSdk();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止接收push消息
     */
    public synchronized void stop() {
        log.info("stop()");
        if (mPushService != null) {
            try {
                mPushService.stop();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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
        boolean isConnected = false;
        if (mPushService != null) {
            try {
                isConnected = mPushService.isConnected();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return isConnected;
    }

    /**
     * @return 返回deviceId
     */
    public synchronized String getDeviceid() {
        if (mPushService != null) {
            try {
                 return mPushService.getDeviceid();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 开始Push服務
     *
     * @param context      context
     */
    public synchronized void startPushService(final Context context) {
        log.info("startPushService()");
        mContext = context.getApplicationContext();
        if (mPushService == null) {
            Intent intent = new Intent(mContext, PushService.class);
            ComponentName componentName = mContext.startService(intent);
            boolean ret = mContext.bindService(intent, mServiceConnection, Context.BIND_IMPORTANT);
        }
    }

}
