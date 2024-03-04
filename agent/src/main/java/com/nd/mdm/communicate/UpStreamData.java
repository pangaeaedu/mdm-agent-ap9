package com.nd.mdm.communicate;

public class UpStreamData {
    private long mSendTime = 0;
    private String mMsgID = "";
    private long mTTLSeconds = 0;
    private String mContentType = "";
    private String mContent = "";
    private String mTopic;

    public UpStreamData(String topic, long pSendTime, String pMsgID, long pTTLSeconds, String pContentType,
                        String pContent) {
        mSendTime = pSendTime;
        mMsgID = pMsgID;
        mTTLSeconds = pTTLSeconds;
        mContentType = pContentType;
        mContent = pContent;

        mTopic = topic;
    }

    public long getSendTime() {
        return mSendTime;
    }


    public String getMsgID() {
        return mMsgID;
    }


    public long getTTLSeconds() {
        return mTTLSeconds;
    }


    public String getContentType() {
        return mContentType;
    }


    public String getContent() {
        return mContent;
    }

    public String getTopic() {
        return mTopic;
    }
}
