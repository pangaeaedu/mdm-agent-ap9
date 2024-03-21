package com.nd.mdm.communicate;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nd.adhoc.push.adhoc.AdhocPushChannel;
import com.nd.adhoc.push.adhoc.IAdhocPushChannelConnectListener;
import com.nd.adhoc.push.adhoc.sdk.PushSdkModule;
import com.nd.adhoc.push.client.libpushclient;
import com.nd.adhoc.push.core.IPushChannel;
import com.nd.adhoc.push.core.IPushChannelConnectListener;
import com.nd.adhoc.push.core.IPushChannelDataListener;
import com.nd.adhoc.push.core.IPushRecvData;
import com.nd.adhoc.push.core.enumConst.PushConnectStatus;
import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.android.adhoc.basic.log.Logger;
import com.nd.sdp.android.serviceloader.AnnotationServiceLoader;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observer;
import rx.schedulers.Schedulers;

public class PushModule {
    private static final String TAG = "PushModule";
    private Context mContext;
    private List<PushConnectListener> mConnectListeners;
    private List<PushDataOperator> mPushDataOperators = new CopyOnWriteArrayList();
    private IPushChannel mPushChannel = null;
    private CopyOnWriteArrayList<UpStreamData> mUpStreamMsgCache = new CopyOnWriteArrayList<>();
    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(5);

    private void discardTimeoutMsg() {
        //CopyOnWriteArrayList不能通过　iterator删除，直接边循环边删除
        Logger.i(TAG, "discardTimeoutMsg: mUpStreamMsgCache.size = " + mUpStreamMsgCache.size());
        for (UpStreamData data : mUpStreamMsgCache) {
            if (data == null) {
                continue;
            }

            long expireTime = MdmTransferConfig.getRequestTimeout();
            if (System.currentTimeMillis() - data.getSendTime() > expireTime) {
                Logger.i(TAG, "discardTimeoutMsg id:" + data.getMsgID()+ ", topic: " + data.getTopic());
                mUpStreamMsgCache.remove(data);
            } else {
                Logger.i(TAG, "do not need discardTimeoutMsg: msg id:" + data.getMsgID()+ ", topic: " + data.getTopic());
            }
        }
    }

    public int sendUpStreamMsg(String msgid, long ttlSeconds, String contentType, String content) {
        //TODO:
        return sendUpStreamMsg("", msgid, ttlSeconds, contentType, content);
    }

    private void cacheUpStreamMsg(String topic, String msgid, long ttlSeconds, String contentType,
                                  String content) {
        Logger.i(TAG, "cacheUpStreamMsg: msgid =  " + msgid);
        UpStreamData data = new UpStreamData(topic,System.currentTimeMillis(), msgid, ttlSeconds,
                contentType, content);
        mUpStreamMsgCache.add(data);
    }

    public int sendUpStreamMsg(String topic, String msgid, long ttlSeconds, String contentType, String content) {
        PushConnectStatus status = mPushChannel.getCurrentStatus();
        if (TextUtils.isEmpty(msgid)) {
            msgid = UUID.randomUUID().toString();
        }
        Logger.i(TAG, "sendUpStreamMsg: getCurrentStatus =  " + status);
        if (status != PushConnectStatus.Connected) {
            cacheUpStreamMsg(topic, msgid, ttlSeconds, contentType, content);
//            start();
            return 0;
        }

        return PushSdkModule.getInstance().sendUpStreamMsg(topic, msgid, ttlSeconds, contentType, content);
    }

    public String getDeviceToken() {
        return PushSdkModule.getInstance().getDeviceid();
    }

    private void resendMsgThenClearCache() {
        Logger.i(TAG, "resendMsgThenClearCache: mUpStreamMsgCache.size = " + mUpStreamMsgCache.size());
        for (UpStreamData data : mUpStreamMsgCache) {
            if (data == null) {
                continue;
            }

            long expireTime = MdmTransferConfig.getRequestTimeout();
            if (System.currentTimeMillis() - data.getSendTime() < expireTime) {
                Logger.i(TAG, "resendMsgThenClearCache: msg id:" + data.getMsgID() + ", topic: " + data.getTopic());
                sendUpStreamMsg(data.getTopic(), data.getMsgID(), data.getTTLSeconds(), data.getContentType(),
                        data.getContent());
            } else {
                Logger.i(TAG, "do not need resendMsgThenClearCache: msg id:" + data.getMsgID()+", topic: " + data.getTopic());
            }
        }

        mUpStreamMsgCache.clear();
    }

    private synchronized void notifyConnectStatus() {
        Logger.i(TAG, "notifyConnectStatus");

        discardTimeoutMsg();
        //push连上后需判断是否需要内网转外网

        PushConnectStatus status = mPushChannel.getCurrentStatus();

        if (status == PushConnectStatus.Connected) {
            Log.e(TAG, "IsAlternateChannel: " + libpushclient.native_pushIsUsingAlternativeServer());
        }

        for (PushConnectListener listener : mConnectListeners) {
            if (status == PushConnectStatus.Connected) {
                listener.onConnected();
                resendMsgThenClearCache();
            } else {
                listener.onDisconnected();
            }
        }
    }

    private IPushChannelConnectListener mChannelConnectListener = new IAdhocPushChannelConnectListener() {
        @Override
        public void onPushDeviceToken(String deviceToken) {
//            for (PushConnectListener listener : mConnectListeners) {
//                if (listener instanceof IAdhocPushConnectListener) {
//                    ((IAdhocPushConnectListener) listener).onPushDeviceToken(deviceToken);
//                }
//            }
        }

        @Override
        public void onConnectStatusChanged(IPushChannel pChannel, PushConnectStatus pStatus) {
//            Logger.e("yhq", "onConnectStatusChanged:"+pStatus);
//            for (IPushConnectListener listener : mConnectListeners) {
//                if (pStatus == PushConnectStatus.Connected) {
//                    listener.onConnected();
//                } else {
//                    listener.onDisconnected();
//                }
//            }

            notifyConnectStatus();
        }
    };
    private IPushChannelDataListener mChannelDataListener = new IPushChannelDataListener() {
        @Override
        public void onPushDataArrived(IPushChannel pChannel, IPushRecvData pData) {
            try {
                String data = new String(pData.getContent());
                JSONObject object = new JSONObject(data);
                int type = object.optInt("msgtype");

                Logger.i(TAG, "onPushMessage, onPushDataArrived: msgtype = " + type);

                for (PushDataOperator pushDataOperator : mPushDataOperators) {
                    if (pushDataOperator == null) {
                        continue;
                    }

                    if (pushDataOperator.isPushMsgTypeMatche(type)) {
                        pushDataOperator.onPushDataArrived(data, pData.getExtraInfos());
                        break;
                    }

                }

//                if (type == AdhocPushMsgType.Feedback.getValue()) {
//                    doFeedbackCmdReceived(content);
//                } else {
//                    doCmdReceived(content);
//                }
                Logger.d(TAG, "after  onPushMessage:" + data);
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(TAG, "onPushDataArrived error:" + e.toString());
                Logger.d(TAG, "onPushDataArrived error:" + e.toString() +
                        ", with messege:" + new String(pData.getContent()));
            }
        }

        @Override
        public void onMessageSendResult(String pMsgID, int pErrorCode) {
        }
    };

    private void initMessageReceiver() {
        Iterator<PushDataOperator> operatorIterator = AnnotationServiceLoader.load(PushDataOperator.class).iterator();

        while (operatorIterator.hasNext()) {
            mPushDataOperators.add(operatorIterator.next());
        }
    }

    private void initPushChannel() {
        //同一Module无需使用AnnotationServiceLoader调用。
//        Iterator<IPushChannel> iterator = AnnotationServiceLoader.load(IPushChannel.class)
//                .iterator();
//        List<IPushChannel> channels = new ArrayList();
//        while (iterator.hasNext()) {
//            IPushChannel channel = iterator.next();
//            channels.add(channel);
//        }

        List<IPushChannel> channels = new ArrayList();
        channels.add(new AdhocPushChannel());

        if (channels.isEmpty()) {
            throw new RuntimeException("could not load any push channel");
        }

        mPushChannel = channels.get(0);
        mPushChannel.addConnectListener(mChannelConnectListener);
        mPushChannel.addDataListener(mChannelDataListener);
        mPushChannel.init(mContext)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Boolean pBoolean) {
                        Logger.i(TAG, "init push channel result:" + pBoolean);
                    }
                });
    }

    public PushModule() {
        Log.i(TAG, "init push module");
        mContext = AdhocBasicConfig.getInstance().getAppContext();
        mConnectListeners = new CopyOnWriteArrayList();

        initMessageReceiver();
        initPushChannel();
    }

    public void fireConnectatusEvent() {
        notifyConnectStatus();
    }

    public void start() {

        Logger.i(TAG, "do start");
        mPushChannel.start()
                .observeOn(Schedulers.from(mExecutorService))
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean pBoolean) {

                    }
                });
    }
}
