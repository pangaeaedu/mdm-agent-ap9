package com.example.mdm_agent_ap9.communicate.push;

public interface IPushModule {

    boolean isConnected();

    int getChannelType();

    void start();

    void stop();

    void setAutoStart(boolean pAutoStart);

    String getDeviceId();

    void fireConnectatusEvent();


    void release();

    int sendUpStreamMsg(String msgid, long ttlSeconds, String contentType, String content);

    int sendUpStreamMsg(String topic, String msgid, long ttlSeconds, String contentType, String content);

    void setAlternatePrefix(String prefix);

    boolean IsAlternateChannel();

}
