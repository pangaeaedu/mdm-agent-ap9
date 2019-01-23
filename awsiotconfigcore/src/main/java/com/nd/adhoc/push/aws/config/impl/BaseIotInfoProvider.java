package com.nd.adhoc.push.aws.config.impl;

import com.nd.adhoc.push.aws.config.IEnvInfoProvider;
import com.nd.adhoc.push.aws.config.IIotInfoProvider;
import com.nd.adhoc.push.aws.config.IMqttInfoProvider;
import com.nd.adhoc.push.aws.config.ITransInfoProvider;


public abstract class BaseIotInfoProvider implements IIotInfoProvider {
    protected IEnvInfoProvider mEnvInfoProvider = null;
    protected ITransInfoProvider mTransInfoProvider = null;
    protected IMqttInfoProvider mMqttInfoProvider = null;

}
