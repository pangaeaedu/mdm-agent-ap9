package com.nd.adhoc.push.core;

public class PushRecvDataImpl implements IPushRecvData {

    private byte[] mData = null;

    public PushRecvDataImpl(byte[] pData){
        mData = pData;
    }

    @Override
    public byte[] getContent() {
        return mData;
    }
}
