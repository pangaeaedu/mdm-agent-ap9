package com.nd.adhoc.push.core;

import java.util.HashMap;
import java.util.Map;

public class PushRecvDataImpl implements IPushRecvData {

    private final byte[] mData;
    private final Map<String,String> mExtraInfos;

    public PushRecvDataImpl(byte[] pData) {
        this(pData, null, null);
    }

    public PushRecvDataImpl(byte[] data, String[] extraKeys, String[] extraValues) {
        mData = data;
        if (AdhocDataCheckUtils.isArrayEmpty(extraKeys) || AdhocDataCheckUtils.isArrayEmpty(extraValues)
                || extraKeys.length != extraValues.length) {
            mExtraInfos = null;
        } else {
            mExtraInfos = new HashMap<>();
            for (int i = 0; i < extraKeys.length; i++) {
                mExtraInfos.put(extraKeys[i], extraValues[i]);
            }
        }
    }

    @Override
    public byte[] getContent() {
        return mData;
    }

    @Override
    public Map<String, String> getExtraInfos() {
        return mExtraInfos;
    }
}
