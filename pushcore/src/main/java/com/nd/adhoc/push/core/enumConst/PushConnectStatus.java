package com.nd.adhoc.push.core.enumConst;

public enum PushConnectStatus {

    Disconnected(0),
    Connecting(1),
    Connected(2);

    private int mValue = 0;

    PushConnectStatus(int pValue){
        mValue = pValue;
    }

    public int getValue(){
        return mValue;
    }
}
