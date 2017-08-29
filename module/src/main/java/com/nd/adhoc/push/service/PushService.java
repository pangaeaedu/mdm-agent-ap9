package com.nd.adhoc.push.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

import com.nd.adhoc.push.PushSdkCallback;
import com.nd.adhoc.push.module.PushSdkModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by XWQ on 2017/8/29 0029.
 * Service(服务)是一个一种可以在后台执行长时间运行操作而没有用户界面的应用组件。服务可由其他应用组件启动（如Activity），
 * 服务一旦被启动将在后台一直运行，即使启动服务的组件（Activity）已销毁也不受影响。
 * 此外，组件可以绑定到服务，以与之进行交互，甚至是执行进程间通信 (IPC)。
 * 例如，服务可以处理网络事务、播放音乐，执行文件 I/O 或与内容提供程序交互，而所有这一切均可在后台进行
 * <p>
 * PushService(Push服务)主要用來保證被启动后将在后台一直运行，即使启动服务的组件（Activity）已销毁也不受影响。
 * 也尝试在该应用被强制删除后，自动唤起 。
 */

public class PushService extends Service {
    private static Logger log = LoggerFactory.getLogger(PushService.class.getSimpleName());
    PushSdkModule mPushSdkModule = new PushSdkModule();

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
        return super.onStartCommand(intent, flags, startId);
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
        return binder;
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

    private PushServiceBinder binder = new PushServiceBinder();

    /**
     * 创建Binder对象，返回给客户端即Activity使用，提供数据交换的接口
     */
    public class PushServiceBinder extends Binder {
        // 声明一个方法，getService。（提供给客户端调用）
        public PushService getService() {
            // 返回当前对象LocalService,这样我们就可在客户端端调用Service的公共方法了
            return PushService.this;
        }
    }

    private BroadcastReceiver mReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    restartPushSdk();
                }
            };

    /**
     * 开始接收Push消息
     *
     * @param context      context
     * @param appid        从Push后台申请的appId
     * @param ip           push服务的IP
     * @param port         push服务的端口
     * @param pushCallback 消息到来的回调
     */
    public void startPushSdk(final Context context, String appid, String ip, int port, PushSdkCallback pushCallback) {
        mPushSdkModule.startPushSdk(context, appid, ip, port, pushCallback);
    }

    /**
     * 断开并重新连接push服务
     */
    public void restartPushSdk() {
        mPushSdkModule.restartPushSdk();
    }

    /**
     * 停止接收push消息
     */
    public void stop() {
        mPushSdkModule.stop();
    }

    /**
     * @return 返回是否与push服务连接着
     */
    public boolean isConnected() {
        return mPushSdkModule.isConnected();
    }

    /**
     * @return 返回deviceId
     */
    public String getDeviceid() {
        return mPushSdkModule.getDeviceid();
    }

    public void notifyClientConnectStatus(boolean isConnected) {
        mPushSdkModule.notifyClientConnectStatus(isConnected);
    }

    public void notifyDeviceToken(String deviceToken) {
        mPushSdkModule.notifyDeviceToken(deviceToken);
    }

    public void notifyPushMessage(long msgId, long msgTime, byte[] data) {
        mPushSdkModule.notifyPushMessage(msgId, msgTime, data);
    }

}
