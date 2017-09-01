package com.nd.adhoc.push.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.nd.sdp.adhoc.push.IDaemonService;
import com.nd.sdp.adhoc.push.IPushService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

/**
 * Created by XWQ on 2017/8/31 0031.
 */

public class DaemonService extends Service {
    private static Logger log = LoggerFactory.getLogger(PushService.class.getSimpleName());
    private IPushService mPushService;

    @Override
    public void onCreate() {
        super.onCreate();
        log.info("DaemonService onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.info("DaemonService onStartCommand()");
        // 如果Service被终止
        // 当资源允许情况下，重启service
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.info("DaemonService onDestroy()");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        log.info("DaemonService onLowMemory()");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        log.info("DaemonService onLowMemory() level = {}", level);
    }

    @Override
    public IBinder onBind(Intent intent) {
        log.info("DaemonService onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        log.info("DaemonService onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        log.info("DaemonService onRebind()");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        log.info("DaemonService onTaskRemoved()");
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /**
         * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
         * 通过这个IBinder对象，实现宿主和Service的交互。
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            log.info("DaemonService ServiceConnection onServiceConnected() ComponentName = " + name);
            // 获取Binder
            mPushService = IPushService.Stub.asInterface(binder);
            if (mPushService != null) {
                try {
                    boolean ret = mPushService.isConnected();
                    log.info("DaemonService ServiceConnection onServiceConnected() isConnected = " + ret);
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
            log.info("DaemonService ServiceConnection onServiceDisconnected() ComponentName = " + name);
            mPushService = null;
            startMonitorPushService();
        }
    };

    private IDaemonService.Stub mBinder = new IDaemonService.Stub() {
        WeakReference<DaemonService> mDaemonService = new WeakReference<DaemonService>(DaemonService.this);

        @Override
        public void startMonitorPushService() throws RemoteException {
            mDaemonService.get().startMonitorPushService();
        }
    };

    /**
     * 开始Push服務
     */
    private void startMonitorPushService() {
        log.info("DaemonService startMonitorPushService()");
        Context appContext = getApplicationContext();
        if (mPushService == null) {
            Intent intent = new Intent(appContext, PushService.class);
            ComponentName componentName = appContext.startService(intent);
            boolean ret = appContext.bindService(intent, mServiceConnection, Context.BIND_IMPORTANT);
        }
    }
}
