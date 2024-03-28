package com.nd.mdm.command;

public enum CommandType {
    //从profile中读取的
    CMD_TYPE_PROFILE_MODULE(0),

    //标准指令---需要显示的
    CMD_TYPE_STATUS(1);

    private int mValue;

    CommandType(int pValue) {
        mValue = pValue;
    }

    public int getValue() {
        return mValue;
    }
}
