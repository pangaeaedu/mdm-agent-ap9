package com.nd.adhoc.push.aws.config;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

//
public interface IMqttInfoProvider {
    int getMqttAliveSec();
    String getLastWillTopic();
    String getLastWillMessage();
    AWSIotMqttQos getLastWillQos();
}
