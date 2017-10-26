package com.nd.adhoc.push;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.nd.adhoc.push.service.PushService;
import com.nd.sdp.adhoc.push.IPushSdkCallback;
import com.nd.sdp.adhoc.push.IPushService;

import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;


public class PushSdk {
    private static PushSdk instance = new PushSdk();

    private static Logger log = Logger.getLogger(PushSdk.class.getSimpleName());

    private static final int START_PUSH_PROCESS = 0;
    private static final int START_PUSH_SDK = 1;
    private static final int RESTART_PUSH_SDK = 2;

    private Context mContext;

    private String mIp;

    private int mPort;

    private String mAppid;

    private String mLoadbanalcerHost = "";

    private int mLoadbanalcerPort = 0;

    private IPushSdkCallback mPushCallback;

    private HandlerThread mHandlerThread ;

    private Handler mHandler;

    private AtomicBoolean mIsInit = new AtomicBoolean(false);

    public PushSdk () {
        //创建一个线程,线程名字：handler-thread
        mHandlerThread = new HandlerThread( "handler-thread");
        //开启一个线程
        mHandlerThread.start();
        //在这个线程中创建一个handler对象
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case START_PUSH_PROCESS:
                        startPushService(mContext);
                        break;
                    case START_PUSH_SDK:
                        startPushServiceInThread();
                        mIsInit.set(true);
                        break;
                    case RESTART_PUSH_SDK:
                        restartPushSdkInThread();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    protected void finalize() throws Throwable {
        //释放资源
        mHandlerThread.quit() ;
        super.finalize();
    }

    private synchronized void startPushServiceInThread() {
        log.info("startPushServiceInThread()");
        if (mPushService != null) {
            try {
                mPushService.startPushSdk(mAppid, mLoadbanalcerHost, mLoadbanalcerPort, mIp, mPort, mPushCallback);
            } catch (RemoteException e) {
                log.info("PushService exception = " + e.toString());
            }
        }
    }

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
            mHandler.removeMessages(START_PUSH_SDK);
            mHandler.sendEmptyMessage(START_PUSH_SDK);
        }

        /**
         * 当取消绑定的时候被回调。但正常情况下是不被调用的，它的调用时机是当Service服务被意外销毁时，
         * 例如内存的资源不足时这个方法才被自动调用。
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            log.info("PushSdk mServiceConnection onServiceDisconnected()");
            mIsInit.set(false);
            mPushService = null;
            boolean isNotifySuccess = false;
            try {
                mPushCallback.onPushShutdown();
                isNotifySuccess = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (mContext != null && isNotifySuccess == false) {
                startPushService(mContext);
            }
        }
    };

    private IPushService mPushService;

    public static PushSdk getInstance() {
        return instance;
    }

    /**
     * 设置负载均衡服务
     *
     * @param host      负载均衡服务地址
     * @param port      负载均衡服务端口
     */
    public synchronized void setLoadBalancer(String host, int port) {
        log.info("setLoadBalancer( "+ host + " , " + port + " ) ");
        mLoadbanalcerHost = host;
        mLoadbanalcerPort = port;
        if (mPushService != null) {
            try {
                mPushService.setLoadbalancer(host, port);
            } catch (RemoteException e) {
                log.info("setLoadBalancer( "+ host + " , " + port + " ) " + " , exception = " + e.toString() );
            }
        }
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
            mHandler.removeMessages(START_PUSH_PROCESS);
            mHandler.sendEmptyMessage(START_PUSH_PROCESS);
        } else {
            mHandler.removeMessages(START_PUSH_SDK);
            mHandler.sendEmptyMessage(START_PUSH_SDK);
        }
    }

    /**
     * 断开并重新连接push服务
     */
    public synchronized void restartPushSdk() {
        log.info("restartPushSdk()");
        mHandler.removeMessages(RESTART_PUSH_SDK);
        mHandler.sendEmptyMessage(RESTART_PUSH_SDK);
    }

    private synchronized void restartPushSdkInThread() {
        log.info("restartPushSdkInThread()");
        if (mPushService != null) {
            try {
                mPushService.restartPushSdk();
            } catch (RemoteException e) {
                log.info("restartPushSdk exception = " + e.toString());
            }
        }
    }

    /**
     * 停止接收push消息
     */
    public synchronized void stop() {
        log.info("stop()");
        mIsInit.set(false);
        if (mPushService != null) {
            try {
                mPushService.stop();
            } catch (RemoteException e) {
                log.info("mPushService.stop() exception = " + e.toString());
            }
        }
        if (mContext != null) {
            if (mServiceConnection != null) {
                try {
                    mContext.unbindService(mServiceConnection);
                } catch (Exception e) {
                    log.info("mContext.unbindService exception = " + e.toString());
                }
            }
        }
        mPushService = null;
    }

    /**
     * @return 返回是否与push服务连接着
     */
    public boolean isConnected() {
        boolean isConnected = false;
        if (mIsInit.get() == true && mPushService != null) {
            try {
                isConnected = mPushService.isConnected();
            } catch (RemoteException e) {
                log.info("isConnected exception = " + e.toString());
            }
        }
        return isConnected;
    }

    /**
     * @return 返回deviceId
     */
    public String getDeviceid() {
        if (mIsInit.get() == true && mPushService != null) {
            try {
                 return mPushService.getDeviceid();
            } catch (RemoteException e) {
                log.info("getDeviceid exception = " + e.toString());
            }
        }
        return null;
    }

    /**
     * 开始Push服務
     *
     * @param context      context
     */
    private synchronized void startPushService(final Context context) {
        log.info("startPushService()");
        mContext = context.getApplicationContext();
        if (mPushService == null) {
            Intent intent = new Intent(mContext, PushService.class);
            ComponentName componentName = mContext.startService(intent);
            boolean ret = mContext.bindService(intent, mServiceConnection, Context.BIND_IMPORTANT);
        }
    }

}
