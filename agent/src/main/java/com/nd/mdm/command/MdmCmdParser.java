package com.nd.mdm.command;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.nd.android.adhoc.basic.common.exception.AdhocException;
import com.nd.mdm.basic.ErrorCode;
import com.nd.mdm.basic.MsgCode;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class MdmCmdParser {

    private static final String KEY_CMD_NAME = "cmd";

    /**
     * 命令解析
     *
     * @param pCmdMsg  命令内容
     * @param pFrom    命令发送方
     * @param pTo      命令结果接收方
     * @param pCmdType 命令类型
     * @return ICmdContent
     */
    @NonNull
    public static MdmCmdContent commandParsing(@NonNull String pCmdMsg,
                                                 @NonNull CommandFromTo pFrom,
                                                 @NonNull CommandFromTo pTo,
                                                 @NonNull CommandType pCmdType) throws AdhocException {

        final JSONObject jsonObject;
        final String sessionId;
        final String newSessionId;
        final String cmdName;
        final long retryTime;
        final long retryInterval;
        final int retryCount;
        final long delaytime;
        final long sendVersion;
        final int orderly;
        final long createTime;

        // from 不允许为未知
        if (CommandFromTo.MDM_CMD_UNKNOW == pFrom) {
            throw new AdhocException("Command's From is UNKNOW", ErrorCode.FAILED, MsgCode.ERROR_PARAMETER);
        }

        // to 不允许为未知，如果 to 是未知，默认设置成 from
        if (CommandFromTo.MDM_CMD_UNKNOW == pTo) {
            pTo = pFrom;
        }

        try {
            jsonObject = new JSONObject(pCmdMsg);
            sessionId = jsonObject.optString(CommandConfig.KEY_SESSIONID, "");

        } catch (JSONException e) {
            throw new AdhocException(ExceptionUtils.getStackTrace(e), ErrorCode.FAILED, MsgCode.ERROR_JSON_INVALID);
        }

        if (TextUtils.isEmpty(sessionId)) {
            throw new AdhocException("sessionId is empty", ErrorCode.FAILED, MsgCode.ERROR_PARAMETER);
        }

        cmdName = jsonObject.optString(KEY_CMD_NAME, "").toLowerCase();

        newSessionId = !TextUtils.isEmpty(sessionId) ? sessionId : jsonObject.optString(CommandConfig.KEY_SESSIONID, "");
        delaytime = jsonObject.optLong("delaytime");

        retryTime = jsonObject.optLong("expire_time");
        retryInterval = jsonObject.optLong("retry_interval");
        retryCount = jsonObject.optInt("retry_num");
        sendVersion = jsonObject.optLong("send_version");

        orderly = jsonObject.optInt("orderly");
        createTime = jsonObject.optLong("createTime");

        // 测试代码
//            retryTime = jsonObject.optLong("retry_time", System.currentTimeMillis() + 6 * 24 * 60 * 60 * 1000); // 6天
//            retryInterval = jsonObject.optLong("retry_interval", 60 * 1000); // 1分钟重试一次
//            retryCount = jsonObject.optInt("retry_num", 5); // 重试次数

        if (TextUtils.isEmpty(cmdName)) {
            throw new AdhocException("cmdName is empty", ErrorCode.FAILED, MsgCode.ERROR_PARAMETER);
        }

        return new MdmCmdContent(cmdName, jsonObject, newSessionId, pFrom.getValue(), pTo.getValue(), pCmdType.getValue())
                .setDelayTime(delaytime)
                .setRetryCount(retryCount)
                .setRetryTime(retryTime).
                setRetryInterval(retryInterval)
                .setSendVersion(sendVersion)
                .setOrderly(orderly)
                .setCreateTime(createTime);
    }
}

