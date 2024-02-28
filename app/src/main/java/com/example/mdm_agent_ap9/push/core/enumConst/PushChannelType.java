package com.example.mdm_agent_ap9.push.core.enumConst;

public enum PushChannelType {

    Aws(12),
    Push(1);

    private int mValue = 0;

    PushChannelType(int pValue){
        mValue = pValue;
    }

    public int getValue(){
        return mValue;
    }
}
