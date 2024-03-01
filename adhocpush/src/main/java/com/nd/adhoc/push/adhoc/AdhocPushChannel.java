package com.nd.adhoc.push.adhoc;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import android.util.Log;

import com.nd.adhoc.push.adhoc.sdk.PushSdkModule;
import com.nd.adhoc.push.core.BasePushChannel;
import com.nd.adhoc.push.core.IPushChannel;
import com.nd.adhoc.push.core.IPushChannelConnectListener;
import com.nd.adhoc.push.core.IPushChannelDataListener;
import com.nd.adhoc.push.core.IPushLoginResultListener;
import com.nd.adhoc.push.core.IPushSendData;
import com.nd.adhoc.push.core.IPushSendResult;
import com.nd.adhoc.push.core.PushRecvDataImpl;
import com.nd.adhoc.push.core.enumConst.PushConnectStatus;
import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.android.adhoc.basic.log.CrashAnalytics;
import com.nd.android.adhoc.basic.log.Logger;
import com.nd.sdp.adhoc.push.IPushSdkCallback;
import com.nd.sdp.android.serviceloader.annotation.Service;

import java.util.UUID;

import rx.Observable;
import rx.functions.Func1;

@Service(IPushChannel.class)
public class AdhocPushChannel extends BasePushChannel {
    private static final String TAG = "AdhocPushChannel";
    private String mUid = "";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PushSdkModule.getInstance().triggetReconnect();
        }
    };

    @Override
    public Observable<Boolean> init(@NonNull Context pContext) {
        return super.init(pContext)
                .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean pBoolean) {
                        return start();
                    }
                });
    }

    @Override
    public int getChannelType() {
        return 1;
    }

    @Override
    public void uninit() {
        mConnectListeners.clear();
        mDataListeners.clear();
        mUid = "";
    }

    @Override
    public void setAutoStart(boolean pStart) {
        PushSdkModule.getInstance().setAutoStart(pStart);
    }

    @Override
    public Observable<Boolean> start() {
//        if (getCurrentStatus() == PushConnectStatus.Connected) {
//            PushSdkModule.getInstance().stop();
//        }

//        CrashAnalytics.INSTANCE.reportException(new Exception("AdhocPushChannel start"));
        Log.e(TAG, "AdhocPushChannel start");
        final Context context = AdhocBasicConfig.getInstance().getAppContext();

//        if (ActivityCompat.checkSelfPermission(AdhocBasicConfig.getInstance().getAppContext(),
//                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startAdhocPush(context, mPushSdkCallback);
            return Observable.just(true);
//        } else {
//            return AdhocRxPermissions.getInstance(context)
//                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    .map(new Func1<Boolean, Boolean>() {
//                        @Override
//                        public Boolean call(Boolean pBoolean) {
//                            if (pBoolean) {
//
//                                startAdhocPush(context, mPushSdkCallback);
//                                return true;
//                            }
//
////                        CrashAnalytics.INSTANCE
////                                .reportException(new Exception("start adhoc push failed, do not have write external " +
////                                "storage permission"));
//                            Log.e(TAG, "start adhoc push failed, do not have write external storage permission");
//                            return false;
//                        }
//                    });
//        }

    }


    private void startAdhocPush(final Context pContext, IPushSdkCallback pushCallback) {
//                "name": "Develop",
//                "org": "mdm",
//                "pushlbs": "http://iot-api.dev.101.com:1770/v5/sdk/access",
//                "puship": "172.24.140.51",
//                "pushport": 1730,
//                "push_app_id":"mdm",
//                "push_app_key":"oy1qjpP2FCuaYrmxXoaPcjV",
//                "url": "http://drms.dev.web.nd",
//                "downloadservicename":"dev_content_drms",
//                "apm_server":"http://qc-report-collection.dev.web.nd/v1",
//                "exception_report_host_name":"DEV",
//                "cs_base_url":"https://betacs.101.com/v0.1/",
//                "cs_base_down_url":"https://betacs.101.com/v0.1/",
//                "uc_protocol_update_url":"",
//                "uc_new_version_base_url":"https://uc-gateway.beta.101.com/v1.1/",
//                "uc_app_id":"bc954f54-b9b9-4fae-97d2-b6a4085c3014",

        String pushIp = "172.24.140.51";
        int pushSrvPort = 1730;
        String pushLbsUrl = "http://iot-api.dev.101.com:1770/v5/sdk/access";
        String pushAppID = "mdm";
        String pushAppKey = "oy1qjpP2FCuaYrmxXoaPcjV";
        PushSdkModule.getInstance().setDefaultServerAddr(pushIp,pushSrvPort);
        Logger.d(TAG, "startAdhocPush appid:" + pushAppID + " appKey:" + pushAppKey + " url:" + pushLbsUrl );
        PushSdkModule.getInstance().startPushSdk(pContext, pushAppID, pushAppKey, pushLbsUrl, pushCallback);
        try {
            if (pContext != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                pContext.registerReceiver(mReceiver, filter);
            }
        } catch (Exception e) {
            CrashAnalytics.INSTANCE.reportException(e);
        }

    }

    @Override
    public Observable<Boolean> stop() {
        // add by winnyang at 20190110 根据自主网的业务需求，哪怕是登出，也不能断开Push
        // 因为有可能在登出的情况下，通过下发指令来执行某些操作
        //  PushSdkModule.getInstance().stop();
        return Observable.just(true);
    }

    @Override
    public String getPushID() {
        return PushSdkModule.getInstance().getDeviceid();
    }

    @Override
    public PushConnectStatus getCurrentStatus() {
        if (PushSdkModule.getInstance().isConnected()) {
            return PushConnectStatus.Connected;
        } else {
            return PushConnectStatus.Disconnected;
        }
    }

    @Override
    public IPushSendResult send(IPushSendData pData) {
        Log.e(TAG, "current version of adhoc push does not support send data");
        return null;
    }

    private void notifyConnectStatus(boolean isConnected) {
        if (isConnected) {
            for (IPushChannelConnectListener listener : mConnectListeners) {
                listener.onConnectStatusChanged(this, PushConnectStatus.Connected);
            }
        } else {
            for (IPushChannelConnectListener listener : mConnectListeners) {
                listener.onConnectStatusChanged(this, PushConnectStatus.Disconnected);
            }
        }
    }


    private IPushSdkCallback.Stub mPushSdkCallback = new IPushSdkCallback.Stub() {
        @Override
        public void onPushDeviceToken(String deviceToken) {
            Logger.d(TAG, "onPushDeviceToken :" + deviceToken);
            for (IPushChannelConnectListener listener : mConnectListeners) {
                if (listener instanceof IAdhocPushChannelConnectListener) {
                    ((IAdhocPushChannelConnectListener) listener).onPushDeviceToken(deviceToken);
                }
            }
        }

        @Override
        public byte[] onPushMessage(String appId, int msgtype, byte[] contenttype,
                                    long msgid, long msgTime, String topic, byte[] content,
                                    String[] extraKeys, String[] extraValues) {
            try {
                for (IPushChannelDataListener listener : mDataListeners) {
                    PushRecvDataImpl data = new PushRecvDataImpl(content, extraKeys, extraValues);
                    listener.onPushDataArrived(AdhocPushChannel.this, data);
                }
            } catch (Exception e) {
                CrashAnalytics.INSTANCE.reportException(e);
                e.printStackTrace();
            }

            return UUID.randomUUID().toString().getBytes();
        }

        @Override
        public void onPushStatus(final boolean isConnected) {
            Log.e(TAG, "onPushStatus :" + isConnected);
            notifyConnectStatus(isConnected);
        }

        @Override
        public void onPushShutdown() throws RemoteException {
            start();
        }

        @Override
        public void onPushUpstreamSent(String msgId, int errCode) throws RemoteException {
            try {
                for (IPushChannelDataListener listener : mDataListeners) {
                    listener.onMessageSendResult(msgId, errCode);
                }
            } catch (Exception e) {
                CrashAnalytics.INSTANCE.reportException(e);
                e.printStackTrace();
            }
        }

        @Override
        public void onPushShadowUpdated(int mode, String document) throws RemoteException {

        }

        @Override
        public void onPushLoginResult(int pCode) throws RemoteException {
            try {
                for (IPushLoginResultListener listener : mLoginResultListeners) {
                    listener.onLoginResult(pCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public String getUid() {
        if (mUid == null) {
            String resLine = CmdUtil.runCmd("cat /proc/cpuinfo");
            String hardware = null;
            String serial = null;
            String[] lines = resLine.split("\n");
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].indexOf("Hardware") != -1) {
                    hardware = lines[i].substring(lines[i].indexOf(":") + 1, lines[i].length());
                    break;
                }
            }
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].indexOf("Serial") != -1) {
                    serial = lines[i].substring(lines[i].indexOf(":") + 1, lines[i].length());
                    break;
                }
            }
            hardware = hardware != null ? hardware.trim() : null;
            serial = serial != null ? serial.trim() : null;
            if (hardware != null && serial != null && hardware.length() + serial.length() > 63) {
                hardware = hardware.substring(0, 63 - serial.length());
            }
            mUid = String.valueOf(hardware) + "-" + String.valueOf(serial);
        }
        return mUid;
    }
}
