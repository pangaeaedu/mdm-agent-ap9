package com.nd.adhoc.push.aws.config.promethean;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.nd.adhoc.assistant.sdk.deviceInfo.DeviceHelper;
import com.nd.adhoc.push.aws.config.IEnvInfoProvider;
import com.nd.adhoc.push.aws.config.IIotInfoProvider;
import com.nd.adhoc.push.aws.config.IMqttInfoProvider;
import com.nd.adhoc.push.aws.config.ITransInfoProvider;
import com.nd.adhoc.push.aws.config.impl.BaseIotInfoProvider;
import com.nd.adhoc.push.aws.config.impl.EnvInfoProviderImpl;
import com.nd.adhoc.push.aws.config.impl.MqttInfoProviderImpl;
import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.sdp.android.serviceloader.annotation.Service;

@Service(IIotInfoProvider.class)
public class PrometheanIotInfoProvider extends BaseIotInfoProvider implements IIotInfoProvider {
    public PrometheanIotInfoProvider(){
    }

    @Override
    public String createClientID() {
        return DeviceHelper.getDeviceToken();
    }

    @Override
    public IEnvInfoProvider getEnvInfoProvider() {
        if(mEnvInfoProvider == null){
            Region region = Region.getRegion(Regions.US_WEST_2);
            String dirPath = AdhocBasicConfig.getInstance().getAppContext().getFilesDir().getPath();
            mEnvInfoProvider = new EnvInfoProviderImpl("a3n57i6h7fkfle-ats.iot.us-west-2.amazonaws.com",
                    "yangTestPolicy",region, dirPath, "iot_keystore",
                    "password", "default");
        }

        return mEnvInfoProvider;
    }

    @Override
    public ITransInfoProvider getTransInfoProvider() {
        if(mTransInfoProvider == null){
            mTransInfoProvider = new TransInfoProviderForMdm();
        }

        return mTransInfoProvider;
    }

    @Override
    public IMqttInfoProvider getMqttInfoProvider() {
        if(mMqttInfoProvider == null){
            mMqttInfoProvider = new MqttInfoProviderImpl(10, "my/lwt/topic",
                "Android client lost connection", AWSIotMqttQos.QOS0);
        }

        return mMqttInfoProvider;
    }
}
