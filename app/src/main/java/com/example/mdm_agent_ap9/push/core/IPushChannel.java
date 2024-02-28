package com.example.mdm_agent_ap9.push.core;

import android.content.Context;

import com.example.mdm_agent_ap9.push.core.enumConst.PushConnectStatus;

import rx.Observable;

public interface IPushChannel {

    int getChannelType();

    Observable<Boolean> init(Context pContext);
    void uninit();

    Observable<Boolean> start();
    Observable<Boolean> stop();

    void setAutoStart(boolean pStart);

    String getPushID();

    PushConnectStatus getCurrentStatus();

    IPushSendResult send(IPushSendData pData);

    void addConnectListener(IPushChannelConnectListener pListener);
    void removeConnectListener(IPushChannelConnectListener pListener);

    void addDataListener(IPushChannelDataListener pListener);
    void removeDataListener(IPushChannelDataListener pListener);

    void addLoginResultListener(IPushLoginResultListener pListener);
    void removeLoginResultListener(IPushLoginResultListener pListener);
}
