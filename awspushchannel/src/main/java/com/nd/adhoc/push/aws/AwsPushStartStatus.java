package com.nd.adhoc.push.aws;

public enum AwsPushStartStatus {
    Init(0),
    Proceeding(1),
    Success(2),
    Failed(3);

    private int mValue = 0;

    AwsPushStartStatus(int pValue){
        mValue = pValue;
    }

}
