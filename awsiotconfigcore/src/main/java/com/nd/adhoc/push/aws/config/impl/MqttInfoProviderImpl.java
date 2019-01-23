package com.nd.adhoc.push.aws.config.impl;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.nd.adhoc.push.aws.config.IMqttInfoProvider;

public class MqttInfoProviderImpl implements IMqttInfoProvider {

    protected int mAliveSec;
    protected String mLastWillTopic;
    protected String mLastWillMessage;
    protected AWSIotMqttQos mLastWillQos;

    public MqttInfoProviderImpl(int pAliveSec, String pLastWillTopic,
                                String pLastWillMessage, AWSIotMqttQos pIotMqttQos){
        mAliveSec = pAliveSec;
        mLastWillTopic = pLastWillTopic;
        mLastWillMessage = pLastWillMessage;
        mLastWillQos = pIotMqttQos;
    }

    @Override
    public int getMqttAliveSec() {
        return mAliveSec;
    }

    @Override
    public String getLastWillTopic() {
        return mLastWillTopic;
    }

    @Override
    public String getLastWillMessage() {
        return mLastWillMessage;
    }

    @Override
    public AWSIotMqttQos getLastWillQos() {
        return mLastWillQos;
    }
}
