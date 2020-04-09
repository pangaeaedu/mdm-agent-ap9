package com.nd.adhoc.push.adhoc.sdk;

public enum PushQoS {
    QoS0(0),
    QoS1(1);

    private int intValue;

    PushQoS(int i) {
        this.intValue = i;
    }

    public int getIntValue() {
        return intValue;
    }
}
