package com.example.mdm_agent_ap9.communicate.impl;

import android.util.Log;

import com.example.mdm_agent_ap9.communicate.push.IPushModule;
import com.example.mdm_agent_ap9.push.core.IPushChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observer;
import rx.schedulers.Schedulers;

class PushModule implements IPushModule {
    private static final String TAG = "PushModule";

    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(5);

    private IPushChannel mPushChannel = null;

    PushModule() {
        initMessageReceiver();
        initPushChannel();
    }

    private void initMessageReceiver() {
        Iterator<IPushDataOperator> operatorIterator = AnnotationServiceLoader.load(IPushDataOperator.class).iterator();

        while (operatorIterator.hasNext()) {
            mPushDataOperators.add(operatorIterator.next());
        }
    }

    private void initPushChannel() {
        Iterator<IPushChannel> iterator = AnnotationServiceLoader.load(IPushChannel.class)
                .iterator();
        List<IPushChannel> channels = new ArrayList<>();
        while (iterator.hasNext()) {
            IPushChannel channel = iterator.next();
            channels.add(channel);
        }

        if (channels.isEmpty()) {
            throw new RuntimeException("could not load any push channel");
        }

        mPushChannel = channels.get(0);
        mPushChannel.addConnectListener(mChannelConnectListener);
        mPushChannel.addDataListener(mChannelDataListener);
    }

    @Override
    public boolean isConnected() {
        PushConnectStatus status = mPushChannel.getCurrentStatus();
        if (status == PushConnectStatus.Connected) {
            return true;
        }

        return false;
    }

    @Override
    public int getChannelType() {
        return mPushChannel.getChannelType();
    }

    @Override
    public void start() {

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


    @Override
    public void stop() {
        mPushChannel.stop();
    }

    @Override
    public void setAutoStart(boolean pAutoStart) {
        mPushChannel.setAutoStart(pAutoStart);
    }

    @Override
    public String getDeviceId() {
        return mPushChannel.getPushID();
    }


    @Override
    public void fireConnectatusEvent() {
        notifyConnectStatus();
    }

    @Override
    public int sendUpStreamMsg(String msgid, long ttlSeconds, String contentType, String content) {
        return sendUpStreamMsg("", msgid, ttlSeconds, contentType, content);
    }

    @Override
    public int sendUpStreamMsg(String topic, String msgid, long ttlSeconds, String contentType, String content) {
        PushConnectStatus status = mPushChannel.getCurrentStatus();
        if (status != PushConnectStatus.Connected) {
            cacheUpStreamMsg(topic, msgid, ttlSeconds, contentType, content);
//            start();
            return 0;
        }

        return PushSdkModule.getInstance().sendUpStreamMsg(topic, msgid, ttlSeconds, contentType, content);
    }

    @Override
    public int publish(String topic, String msgid, PushQoS qos, String content) {
        return PushSdkModule.getInstance().publish(topic, msgid, qos, content);
    }

    @Override
    public void subscribe(String topic, PushQoS qos) {
        HashMap<String, PushQoS> topics = new HashMap<>();
        PushSdkModule.getInstance().subscribe(topics);
    }

    @Override
    public void setAlternatePrefix(String prefix) {
        libpushclient.native_pushConfig("{\"alternative_server_prefix\": \"" + prefix + "\"}");
    }

    @Override
    public boolean IsAlternateChannel() {
        return libpushclient.native_pushIsUsingAlternativeServer();
    }

    private void cacheUpStreamMsg(String topic, String msgid, long ttlSeconds, String contentType,
                                  String content) {
        Logger.i(TAG, "cacheUpStreamMsg: msgid =  " + msgid);
        UpStreamData data = new UpStreamData(topic,System.currentTimeMillis(), msgid, ttlSeconds,
                contentType, content);
        mUpStreamMsgCache.add(data);
    }

    private synchronized void notifyConnectStatus() {
        Logger.i(TAG, "notifyConnectStatus");

        discardTimeoutMsg();
        //push连上后需判断是否需要内网转外网

        PushConnectStatus status = mPushChannel.getCurrentStatus();

        if (status == PushConnectStatus.Connected) {
            Log.e(TAG, "IsAlternateChannel: " + IsAlternateChannel());

            //需切换cs相关地址
            IMdmEnvModule module = MdmEvnFactory.getInstance().getCurEnvironment();
            CsManager.setContentBaseUrl(AdhocUrlHandler.handlerUrl(module.getCsBaseUrl()));
            CsBaseManager.setContentBaseUrl(AdhocUrlHandler.handlerUrl(module.getCsBaseUrl()));

            CsManager.setContentDownBaseUrl(AdhocUrlHandler.handlerUrl(module.getCsBaseDownUrl()));
            CsBaseManager.setDownloadBaseUrl(AdhocUrlHandler.handlerUrl(module.getCsBaseDownUrl()));

            Logger.e(TAG, "modify cs CONTENT_GLOBAL_KEY");
            GlobalHttpConfig.bindArgument(CsBaseManager.CONTENT_GLOBAL_KEY, CsBaseManager.toPreHost(AdhocUrlHandler.handlerUrl(module.getCsBaseDownUrl())));
        }
        for (IPushConnectListener listener : mConnectListeners) {
            if (status == PushConnectStatus.Connected) {
                listener.onConnected();
                resendMsgThenClearCache();
            } else {
                listener.onDisconnected();
            }
        }
    }

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
}
