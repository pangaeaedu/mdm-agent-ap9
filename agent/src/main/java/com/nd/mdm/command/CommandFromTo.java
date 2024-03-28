package com.nd.mdm.command;

import androidx.annotation.NonNull;

public enum CommandFromTo {
    MDM_CMD_ADHOC(1),
    MDM_CMD_DRM(2),
    MDM_CMD_DATABASE(3),
    MDM_CMD_UNKNOW(-1);

    private int mValue;

    CommandFromTo(int pValue) {
        mValue = pValue;
    }

    public int getValue() {
        return mValue;
    }

    /**
     * getTypeByValue
     * 根据字符串获取枚举值
     *
     * @param pValue 名称
     * @return MdmCmdFromTo
     */
    @NonNull
    public static CommandFromTo getTypeByValue(int pValue) {
        CommandFromTo[] array = CommandFromTo.values();
        for (CommandFromTo flag : array) {
            if (flag.mValue == pValue) {
                return flag;
            }
        }
        return MDM_CMD_UNKNOW;
    }
}
