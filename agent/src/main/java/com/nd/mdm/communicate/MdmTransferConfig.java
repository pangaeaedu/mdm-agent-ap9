package com.nd.mdm.communicate;

import androidx.annotation.NonNull;

import com.nd.android.adhoc.basic.sp.ISharedPreferenceModel;
import com.nd.android.adhoc.basic.sp.SharedPreferenceFactory;

import java.util.concurrent.atomic.AtomicLong;

public class MdmTransferConfig {
    private static AdhocNetworkChannel sNetworkChannel;

    private static final String SP_REQUEST_TIMEOUT = "push_request_timeout";
    private static final String SP_REQUEST_CHANNEL = "network_request_channel";

    private static final long DEF_REQUEST_TIMEOUT = 20 * 1000;

    private static AtomicLong sRequestTimeout;

    static {
        ISharedPreferenceModel sharedPreferenceModel =
                SharedPreferenceFactory.getInstance().getModel();

        long timeout = sharedPreferenceModel.getLong(SP_REQUEST_TIMEOUT, DEF_REQUEST_TIMEOUT);
        sRequestTimeout = new AtomicLong(timeout);

        int networkChannel = sharedPreferenceModel.getInt(SP_REQUEST_CHANNEL, AdhocNetworkChannel.CHANNEL_PUSH.getValue());
        sNetworkChannel = AdhocNetworkChannel.getTypeByValue(networkChannel);
    }

    public static void setNetworkChannel(@NonNull AdhocNetworkChannel pNetworkChannel) {
        sNetworkChannel = pNetworkChannel;

        ISharedPreferenceModel sharedPreferenceModel =
                SharedPreferenceFactory.getInstance().getModel();
        sharedPreferenceModel.putInt(SP_REQUEST_CHANNEL, getNetworkChannel().getValue()).apply();
    }

    public static AdhocNetworkChannel getNetworkChannel() {
        return sNetworkChannel;
    }

    public static void setRequestTimeout(long pRequestTimeout){
        if (pRequestTimeout <= 0) {
            pRequestTimeout = DEF_REQUEST_TIMEOUT;
        }

        sRequestTimeout.set(pRequestTimeout);

        ISharedPreferenceModel sharedPreferenceModel =
                SharedPreferenceFactory.getInstance().getModel();
        sharedPreferenceModel.putLong(SP_REQUEST_TIMEOUT, getRequestTimeout()).apply();
    }

    public static long getRequestTimeout(){
        return sRequestTimeout.get();
    }
}
