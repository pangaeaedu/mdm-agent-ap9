package com.nd.adhoc.push.aws.config;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

public interface ITransInfoProvider {
    String getPublishTopic();
    AWSIotMqttQos getPublishQos();

    String getSubscribeTopic();
    AWSIotMqttQos getSubscribeQos();
}
