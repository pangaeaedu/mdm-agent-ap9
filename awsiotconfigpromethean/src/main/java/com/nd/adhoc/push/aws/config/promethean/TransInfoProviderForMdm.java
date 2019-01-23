package com.nd.adhoc.push.aws.config.promethean;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.nd.adhoc.assistant.sdk.deviceInfo.DeviceHelper;
import com.nd.adhoc.push.aws.config.ITransInfoProvider;

public class TransInfoProviderForMdm implements ITransInfoProvider {

    @Override
    public String getPublishTopic() {
        return DeviceHelper.getDeviceToken();
    }

    @Override
    public AWSIotMqttQos getPublishQos() {
        return AWSIotMqttQos.QOS0;
    }

    @Override
    public String getSubscribeTopic() {
        return DeviceHelper.getDeviceToken();
    }

    @Override
    public AWSIotMqttQos getSubscribeQos() {
        return AWSIotMqttQos.QOS0;
    }
}
