package com.nd.adhoc.push.adhoc.sdk;

public enum PushShadowMode {
    ShadowModeDevice(0), // 以设备ID作为影子记录ID
    ShadowModeAlias(1);  // 以别名作为影子记录ID

    private static String [] stringValues = {"ShadowModeDevice", "ShadowModeAlias"};
    private int intValue;

    PushShadowMode(int i) {
        this.intValue = i;
    }

    public int getIntValue() {
        return intValue;
    }

    public String  getStringValue() {
        return stringValues[intValue];
    }

    public static PushShadowMode fromIntValue(int i) {
        if (i==0) {
            return ShadowModeDevice;
        } else {
            return ShadowModeAlias;
        }
    }
}
