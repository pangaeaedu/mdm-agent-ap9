package com.nd.adhoc.push.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteException;

import com.nd.adhoc.push.module.PushSdkModule;
import com.nd.sdp.adhoc.push.IDaemonService;
import com.nd.sdp.adhoc.push.IPushSdkCallback;
import com.nd.sdp.adhoc.push.IPushService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

/**
 * Created by XWQ on 2017/8/29 0029.
 * Service(服务)是一个一种可以在后台执行长时间运行操作而没有用户界面的应用组件。服务可由其他应用组件启动（如Activity），
 * 服务一旦被启动将在后台一直运行，即使启动服务的组件（Activity）已销毁也不受影响。
 * 此外，组件可以绑定到服务，以与之进行交互，甚至是执行进程间通信 (IPC)。
 * 例如，服务可以处理网络事务、播放音乐，执行文件 I/O 或与内容提供程序交互，而所有这一切均可在后台进行
 * <p>
 * PushService(Push服务)主要用來保證被启动后将在后台一直运行，即使启动服务的组件（Activity）已销毁也不受影响。
 * 也尝试在该应用被强制删除后，自动唤起 。
 *
 * TODO : Service 中的 IBinder 方法未完, 目前的寫法有下列問題
 *         一. 跨進程通訊未實現 (aidl)
 *             1. 使用方如果使用非主線程方式調用, 有機率發生崩潰
 *             2. 如果將 Service 設成單獨線程, 會有崩潰機率
 *         二. 保活机制未完全實現
 */

public class PushService extends Service {
    private static Logger log = LoggerFactory.getLogger(PushService.class.getSimpleName());
    private IDaemonService mDaemonService;

    private ServiceConnection mDaemonServiceConnection  = new ServiceConnection() {
        /**
         * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
         * 通过这个IBinder对象，实现宿主和Service的交互。
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            log.info("PushService mDaemonServiceConnection onServiceConnected()");
            mDaemonService = IDaemonService.Stub.asInterface(binder);
            if (mDaemonService != null) {
                try {
                    mDaemonService.startMonitorPushService();
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
            log.info("PushService mDaemonServiceConnection onServiceDisconnected()");
            startDaemonService();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        log.info("onCreate()");
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.info("onStartCommand()");
        startDaemonService();
        // 如果Service被终止
        // 当资源允许情况下，重启service
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        log.info("onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        log.info("onLowMemory()");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        log.info("onTrimMemory() level {} ", level);
    }

    @Override
    public IBinder onBind(Intent intent) {
        log.info("onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        log.info("onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        log.info("onRebind()");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        log.info("onTaskRemoved()");
    }

    /**
     * 创建Binder对象，返回给客户端即 Activity或 Service 使用，提供数据交换的接口
     */
    private IPushService.Stub mBinder = new IPushService.Stub() {
        WeakReference<PushService> mPushService = new WeakReference<PushService>(PushService.this);
        @Override
        public void startPushSdk(String appid, String ip, int port, IPushSdkCallback pushCallback) throws RemoteException {
            mPushService.get().startPushSdk(appid, ip, port, pushCallback);
        }

        @Override
        public void restartPushSdk() throws RemoteException {
            mPushService.get().restartPushSdk();
        }

        @Override
        public void stop() throws RemoteException {
            mPushService.get().stop();
        }

        @Override
        public boolean isConnected() throws RemoteException {
            return mPushService.get().isConnected();
        }

        @Override
        public String getDeviceid() throws RemoteException {
            return mPushService.get().getDeviceid();
        }

    };

    private BroadcastReceiver mReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    restartPushSdk();
                }
            };

    private void startDaemonService() {
        log.info("startDaemonService()");
        Intent intentDaemon = new Intent();
        intentDaemon.setClass(this, DaemonService.class);
        ComponentName componentName = startService(intentDaemon);
        log.info("startDaemonService() componentName = " + componentName);
        boolean ret = bindService(intentDaemon, mDaemonServiceConnection, Context.BIND_IMPORTANT);
        log.info("bindDaemonService() ret = " + ret);
    }

    /**
     * 开始接收Push消息
     *
     * @param appid        从Push后台申请的appId
     * @param ip           push服务的IP
     * @param port         push服务的端口
     * @param pushCallback 消息到来的回调
     */
    private void startPushSdk(String appid, String ip, int port, IPushSdkCallback pushCallback) {
        PushSdkModule.getInstance().startPushSdk(this, appid, ip, port, pushCallback);
    }

    /**
     * 断开并重新连接push服务
     */
    private void restartPushSdk() {
        PushSdkModule.getInstance().restartPushSdk();
    }

    /**
     * 停止接收push消息
     */
    private void stop() {
        PushSdkModule.getInstance().stop();
    }

    /**
     * @return 返回是否与push服务连接着
     */
    private boolean isConnected() {
        return PushSdkModule.getInstance().isConnected();
    }

    /**
     * @return 返回deviceId
     */
    private String getDeviceid() {
        return PushSdkModule.getInstance().getDeviceid();
    }

}
