package com.nd.mdm.command;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class MdmCmdContent {
    private String mCmdName;
    private JSONObject mCmdJson;
    private String mSessionId;
    private long mDelayTime;
    private long mRetryTime;
    private long mRetryInterval;
    private int mRetryCount;
    private int mFrom;
    private int mTo;
    private int mCmdType;
    private long mSendVersion;
    private int mOrderly;
    private long mCreateTime;

    MdmCmdContent(@NonNull String pCmdName, @NonNull JSONObject pCmdJson, @NonNull String pSessionId, int pFrom, int pTo, int pCmdType) {
        mCmdName = pCmdName;
        mCmdJson = pCmdJson;
        mSessionId = pSessionId;
        mFrom = pFrom;
        mTo = pTo;
        mCmdType = pCmdType;
    }

    public int getFrom() {
        return mFrom;
    }
    public int getTo() {
        return mTo;
    }
    public int getCmdType() {
        return mCmdType;
    }
    public String getCmdName() { return mCmdName; }
    public JSONObject getCmdJson() {
        return mCmdJson;
    }
    public String getSessionId() {
        return mSessionId;
    }
    public long getRetryTime() {
        return mRetryTime;
    }
    public long getRetryInterval() { return mRetryInterval;}
    public int getRetryCount() {
        return mRetryCount;
    }
    public long getDelayTime() {
        return mDelayTime;
    }
    public long getCreateTime() {
        return mCreateTime;
    }
    public long getSendVersion() {
        return mSendVersion;
    }
    public boolean isOrderly() {
        return mOrderly > 0;
    }

    MdmCmdContent setDelayTime(long delayTime) {
        mDelayTime = delayTime;
        return this;
    }

    MdmCmdContent setRetryCount(int retryCount) {
        mRetryCount = retryCount;
        return this;
    }

    MdmCmdContent setRetryInterval(long retryInterval) {
        mRetryInterval = retryInterval;
        return this;
    }

    MdmCmdContent setRetryTime(long retryTime) {
        mRetryTime = retryTime;
        return this;
    }

    MdmCmdContent setSendVersion(long pSendVersion) {
        mSendVersion = pSendVersion;
        return this;
    }

    MdmCmdContent setOrderly(int orderly) {
        mOrderly = orderly;
        return this;
    }

    MdmCmdContent setCreateTime(long pCreateTime) {
        mCreateTime = pCreateTime;
        return this;
    }
}
