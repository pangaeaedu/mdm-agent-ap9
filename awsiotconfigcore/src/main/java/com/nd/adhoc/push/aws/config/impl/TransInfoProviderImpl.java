package com.nd.adhoc.push.aws.config.impl;


import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.nd.adhoc.push.aws.config.ITransInfoProvider;

public class TransInfoProviderImpl implements ITransInfoProvider {
    protected String mPublishTopic;
    protected String mSubscribeTopic;

    protected AWSIotMqttQos mPublishQos = null;
    protected AWSIotMqttQos mSubscribeQos = null;

    public TransInfoProviderImpl(String pPublishTopic,  AWSIotMqttQos pPublishQos,
                                  String pSubscribeTopic,  AWSIotMqttQos pSubscribeQos){
        mPublishTopic = pPublishTopic;
        mPublishQos = pPublishQos;

        mSubscribeTopic = pSubscribeTopic;
        mSubscribeQos = pSubscribeQos;
    }

    @Override
    public String getPublishTopic() {
        return mPublishTopic;
    }

    @Override
    public AWSIotMqttQos getPublishQos() {
        return mPublishQos;
    }

    @Override
    public String getSubscribeTopic() {
        return mSubscribeTopic;
    }

    @Override
    public AWSIotMqttQos getSubscribeQos() {
        return mSubscribeQos;
    }
}
