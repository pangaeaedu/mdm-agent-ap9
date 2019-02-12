package com.nd.adhoc.push.adhoc;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.nd.adhoc.push.adhoc.sdk.PushSdkModule;
import com.nd.adhoc.push.adhoc.utils.HttpUtil;
import com.nd.adhoc.push.core.BasePushChannel;
import com.nd.adhoc.push.core.IPushChannel;
import com.nd.adhoc.push.core.IPushChannelConnectListener;
import com.nd.adhoc.push.core.IPushChannelDataListener;
import com.nd.adhoc.push.core.IPushSendData;
import com.nd.adhoc.push.core.IPushSendResult;
import com.nd.adhoc.push.core.PushRecvDataImpl;
import com.nd.adhoc.push.core.enumConst.PushConnectStatus;
import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.android.adhoc.basic.log.Logger;
import com.nd.android.mdm.biz.env.MdmEvnFactory;
import com.nd.android.mdm.util.cmd.CmdUtil;
import com.nd.sdp.adhoc.push.IPushSdkCallback;
import com.nd.sdp.android.serviceloader.annotation.Service;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.json.JSONException;
import org.json.JSONObject;

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
            PushSdkModule.getInstance().restartPushSdk();
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
    public Observable<Boolean> start() {
        if (getCurrentStatus() == PushConnectStatus.Connected) {
            PushSdkModule.getInstance().stop();
        }

        final Context context = AdhocBasicConfig.getInstance().getAppContext();
        return RxPermissions.getInstance(context)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .map(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean pBoolean) {
                        if (pBoolean) {
                            String pushSrvIp = MdmEvnFactory.getInstance().getCurEnvironment().getPushIp();
                            int pushSrvPort = MdmEvnFactory.getInstance().getCurEnvironment().getPushPort();

                            startAdhocPush(context, "mdm", null, pushSrvIp,
                                    pushSrvPort, mPushSdkCallback);
                            return true;
                        }

                        Log.e(TAG, "start adhoc push failed, do not have write external " +
                                "storage permission");
                        return false;
                    }
                });

//                .subscribe(new AdhocActionSubscriber<>(new Action1<Boolean>() {
//                    @Override
//                    public void call(Boolean aBoolean) {
//                        if (aBoolean) {
//                            String pushSrvIp = MdmEvnFactory.getInstance().getCurEnvironment().getPushIp();
//                            int pushSrvPort = MdmEvnFactory.getInstance().getCurEnvironment().getPushPort();
//
//                            startAdhocPush(context, "mdm", null, pushSrvIp,
//                                    pushSrvPort, mPushSdkCallback);
//                        } else {
//                            Log.e(TAG, "start adhoc push failed, do not have write external " +
//                                    "storage permission");
//                        }
//                    }
//                }));

    }

    private void startAdhocPush(final Context pContext, String appid, String appKey,
                                String ip, int port, IPushSdkCallback pushCallback) {
        try {
            if (pContext != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                pContext.registerReceiver(mReceiver, filter);
            }
        } catch (Exception e) {
            Log.d("PUSH", "register push sdk broadcastreceiver failed : " + e.toString());
        }

        PushSdkModule.getInstance().startPushSdk(pContext, appid, appKey, ip, port, pushCallback);
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
            Log.e(TAG, "onPushDeviceToken :"+deviceToken);
        }

        @Override
        public byte[] onPushMessage(String appId, int msgtype, byte[] contenttype,
                                    long msgid, long msgTime, byte[] content,
                                    String[] extraKeys, String[] extraValues) {
            try {
                for (IPushChannelDataListener listener : mDataListeners) {
                    PushRecvDataImpl data = new PushRecvDataImpl(content);
                    listener.onPushDataArrived(AdhocPushChannel.this, data);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(TAG, "get error:" + e.toString() + "\n with messege:" + new String(content));
            }

            return UUID.randomUUID().toString().getBytes();
        }

        @Override
        public void onPushStatus(final boolean isConnected) {
            Log.e(TAG, "onPushStatus :"+isConnected);
            notifyConnectStatus(isConnected);

            //  郭文要求
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (isConnected) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("stock_id", getUid());
                            String content = jsonObject.toString();
                            String env = MdmEvnFactory.getInstance().getCurEnvironment().getUrl();
                            HttpUtil.post(env, content, getPushID());
                        } catch (JSONException e) {
                            Logger.e(TAG, "assistant service send stock failed:" + e.getMessage());
                        } catch (Exception e) {
                            Logger.e(TAG, "assistant service send stock failed:" + e.getMessage());
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onPushShutdown() throws RemoteException {
            start();
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
