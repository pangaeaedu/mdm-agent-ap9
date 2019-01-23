package com.nd.adhoc.push.aws.config;

import com.amazonaws.regions.Region;

public interface IEnvInfoProvider {
    String getEndPoint();
    String getPolicyName();
    Region getRegion();

    String getKeystoreName();
    String getKeystorePassword();
    String getCertificateId();
    String getKeystoreFolder();
}
