package com.nd.adhoc.push.aws.config.impl;

import com.amazonaws.regions.Region;
import com.nd.adhoc.push.aws.config.IEnvInfoProvider;

public class EnvInfoProviderImpl implements IEnvInfoProvider {

    protected String mEndpoint = "";
    protected String mPolicyName = "";
    protected Region mRegion = null;
    protected String mKeystoreFolder = "";
    protected String mKeystoreName = "";
    protected String mKeystorePassword = "";
    protected String mCertificateID = "";


    public EnvInfoProviderImpl(String pEndpoint, String pPolicyName,
                                  Region pRegion, String pKeystoreFolder, String pKeystoreName,
                                  String pKeystorePassword, String pCertificateID){
        mEndpoint = pEndpoint;
        mPolicyName = pPolicyName;
        mRegion = pRegion;
        mKeystoreFolder = pKeystoreFolder;
        mKeystoreName = pKeystoreName;
        mKeystorePassword = pKeystorePassword;
        mCertificateID = pCertificateID;
    }


    @Override
    public String getEndPoint() {
        return mEndpoint;
    }

    @Override
    public String getPolicyName() {
        return mPolicyName;
    }

    @Override
    public Region getRegion() {
        return mRegion;
    }

    @Override
    public String getKeystoreName() {
        return mKeystoreName;
    }

    @Override
    public String getKeystorePassword() {
        return mKeystorePassword;
    }

    @Override
    public String getCertificateId() {
        return mCertificateID;
    }

    @Override
    public String getKeystoreFolder() {
        return mKeystoreFolder;
    }
}
