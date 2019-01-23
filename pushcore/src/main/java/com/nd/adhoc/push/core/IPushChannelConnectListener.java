package com.nd.adhoc.push.core;

import com.nd.adhoc.push.core.enumConst.PushConnectStatus;

public interface IPushChannelConnectListener {
    void onConnectStatusChanged(IPushChannel pChannel, PushConnectStatus pStatus);
}
