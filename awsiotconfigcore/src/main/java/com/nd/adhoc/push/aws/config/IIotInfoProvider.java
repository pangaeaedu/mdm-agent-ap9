package com.nd.adhoc.push.aws.config;

public interface IIotInfoProvider {
//    // IoT endpoint
//    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
//    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a3n57i6h7fkfle-ats.iot.us-west-2.amazonaws.com";
//    // Name of the AWS IoT policy to attach to a newly created certificate
//    private static final String AWS_IOT_POLICY_NAME = "yangTestPolicy";
//
//    // Region of AWS IoT
//    private static final Regions MY_REGION = Regions.US_WEST_2;
//    // Filename of KeyStore file on the filesystem
//    private static final String KEYSTORE_NAME = "iot_keystore";
//    // Password for the private key in the KeyStore
//    private static final String KEYSTORE_PASSWORD = "password";
//    // Certificate and key aliases in the KeyStore
//    private static final String CERTIFICATE_ID = "default";

//    String getEndPoint();
//    String getPolicyName();
//    Region getRegion();
//
//    String getKeystoreName();
//    String getKeystorePassword();
//    String getCertificateId();
//    String getKeystoreFolder();

    String createClientID();

    IEnvInfoProvider getEnvInfoProvider();

    ITransInfoProvider getTransInfoProvider();

    IMqttInfoProvider getMqttInfoProvider();
//    int getMqttAliveSec();
//
//    String getPublishTopic();
//    AWSIotMqttQos getPublishQos();
//
//    String getSubscribeTopic();
//    AWSIotMqttQos getSubscribeQos();
}
