package com.nd.adhoc.push.adhoc;

import com.nd.adhoc.push.core.IPushChannelConnectListener;


public interface IAdhocPushChannelConnectListener extends IPushChannelConnectListener {
    void onPushDeviceToken(String deviceToken);
}
