package com.nd.adhoc.push.core;

public interface IPushChannelDataListener {
    void onPushDataArrived(IPushChannel pChannel, IPushRecvData pData);
}
