package com.nd.mdm.basic;

public class ErrorCode {
    public static final int FAILED = -1;
    public static final int SUCCESS = 0;
    public static final int EXECUTING = 999;
    public static final int UNKNOWN = 10000;
    public static final int UNUSABLE = 10001;

    //    public static final int ERR_ARGUMENT_INVALID = 10001;
    public static final int ERR_NOT_SYSTEM_APPLICATION = 10002;
    public static final int ERR_TIMEOUT = 10003;
    public static final int ERR_PACKAGE_NOT_EXIST = 10004;

    public static final int ERR_ACTIVATE_DEVICE_TOKEN_INVALID = -1;
    public static final int ERR_ACTIVATE_USER_TOKEN_INVALID = -2;
    public static final int ERR_ACTIVATE_SIM_INVALID = -3;
    public static final int ERR_ACTIVATE_PUSH_STATUS_CHANGED = -4;
    public static final int ERR_ACTIVATE_PUSH_MODULE_INVALID = -5;
    public static final int ERR_ACTIVATE_TIME_OUT = -6;
    public static final int ERR_ACTIVATE_ILLEGAL_STATE = -7;
    public static final int ERR_ACTIVATE_ILLEGAL_CONTEXT = -8;
    public static final int ERR_ACTIVATE_ILLEGAL_ARGUMENT = -9;
    public static final int ERR_ACTIVATE_PUSH_DISCONNECT = -10;
    public static final int ERR_ACTIVATE_USER_CANCEL = -11;
    public static final int ERR_ACTIVATE_UC_LOGIN_FAILED = -12;
    public static final int ERR_ACTIVATE_PROFILE_FAILED = -13;

    public static final int RESULT_ACTIVATE_SUCCESS = 0;
    public static final int RESULT_ACTIVATE_TOKEN_INVALID = 1;
    public static final int RESULT_ACTIVATE_USER_USED = 2;
    public static final int RESULT_ACTIVATE_DEVICE_USED = 3;
    public static final int RESULT_ACTIVATE_SIM_USED = 4;
}
