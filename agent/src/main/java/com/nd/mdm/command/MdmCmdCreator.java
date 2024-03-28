package com.nd.mdm.command;

import android.content.Context;

import androidx.annotation.NonNull;

import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.android.adhoc.basic.common.exception.AdhocException;
import com.nd.android.adhoc.basic.log.Logger;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class MdmCmdCreator {
    private static final String TAG = "MdmCmdCreator";
    private final Map<String, Class<? extends MdmCmd>> mCmdMap = new HashMap<>();

    public MdmCmdCreator() {
        put("sendmsg", CmdSendMsg.class);
    }

    private void put(@NonNull String pCmdName, @NonNull Class<? extends MdmCmd> pCmdClass) {
        if (mCmdMap.containsKey(pCmdName)) {
            throw new IllegalArgumentException("DeliverCmdFactory regist cmd error! The same command already exists : " + pCmdName);
        }
        mCmdMap.put(pCmdName, pCmdClass);
    }

    public MdmCmd createCmd(@NonNull MdmCmdContent pCmdContent) throws AdhocException {
        Context context = AdhocBasicConfig.getInstance().getAppContext();

        JSONObject json = pCmdContent.getCmdJson();
        try {
            String cmdName = pCmdContent.getCmdName();

            Logger.d(TAG, "factory create cmd:" + cmdName + ", sessionId = " + pCmdContent.getSessionId());
            Class<? extends MdmCmd> cmdClass = getCmdClass(cmdName);

            if (cmdClass != null) {
                Constructor<?> constructor = cmdClass.getConstructor(Context.class, JSONObject.class);
                MdmCmd instance = (MdmCmd) constructor.newInstance(context, json);

                instance.setCmdName(cmdName)
                        .setTo(pCmdContent.getTo())
                        .setFrom(pCmdContent.getFrom())
                        .setCmdType(pCmdContent.getCmdType())
                        .setSessionId(pCmdContent.getSessionId())
                        .setRetryTime(pCmdContent.getRetryTime())
                        .setRetryInterval(pCmdContent.getRetryInterval())
                        .setRetryNum(pCmdContent.getRetryCount())
                        .setDelayTime(pCmdContent.getDelayTime());

                Logger.d(TAG, "create class:" + cmdClass.getCanonicalName() + "'s instance success");
                return instance;

            } else {
                Logger.e(TAG, "make cmd failed, can not find class.");
            }
        } catch (InstantiationException e) {
            doException(pCmdContent, e);
        } catch (NoSuchMethodException e) {
            doException(pCmdContent, e);
        } catch (IllegalAccessException e) {
            doException(pCmdContent, e);
        } catch (InvocationTargetException e) {
            doException(pCmdContent, e);
        }
        return null;
    }

    private void doException(MdmCmdContent pCmdContent, Throwable e) throws AdhocException {
        //TODO: 实现异常消息上报
    }

    protected Class<? extends MdmCmd> getCmdClass(@NonNull String pCmdName) {
        return mCmdMap.get(pCmdName);
    }
}
