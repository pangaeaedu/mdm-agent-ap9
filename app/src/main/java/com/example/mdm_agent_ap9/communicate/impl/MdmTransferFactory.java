package com.example.mdm_agent_ap9.communicate.impl;

import com.example.mdm_agent_ap9.communicate.push.IPushModule;

/**
 * MDM 通信传输层 工厂类
 * <p>
 * Created by HuangYK on 2018/5/4.
 */
public class MdmTransferFactory {

    private volatile static IPushModule mPushModel;

    static {
        init();
    }

    private static void init() {
        if (mPushModel == null) {
            mPushModel = new PushModule();
        }

    }

    public static IPushModule getPushModel() {
        return mPushModel;
    }


    public static void release() {
        mPushModel.release();
    }


}
