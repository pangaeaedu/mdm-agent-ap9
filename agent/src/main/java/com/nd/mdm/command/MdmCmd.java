package com.nd.mdm.command;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.nd.mdm.basic.ErrorCode;
import com.nd.mdm.basic.MsgCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

public class MdmCmd {

    protected Context mContext;

    private JSONObject mCmdJson;

    // 指令的名称
    private String mCmdName = "CMD_NOT_EXIST";

    // 指令业务展示的 名称
    private String mCmdBizName;


    private int mFrom;
    private int mTo;
    private int mCmdType;
    private String mSessionId = CommandConfig.SESSION_ID_NOT_EXIST;

    private int mErrorCode = ErrorCode.UNKNOWN;
    private int mMsgCode = MsgCode.ERROR_NONE;
    private long mStartTime;
    private String mMsg;


    private long mRetryTime;
    private int mRetryNum;
    private long mRetryInterval;

    private long mSendVersion;

    private long mDelayTime;

    private AtomicBoolean mIsReleased = new AtomicBoolean(false);

    public MdmCmd(Context context, JSONObject pCmdJson) {
        mContext = context;
        mCmdJson = pCmdJson;
        mStartTime = System.currentTimeMillis();

        mSendVersion = mCmdJson.optLong("send_version", 0);
    }

    MdmCmd setFrom(int pFrom) {
        mFrom = pFrom;
        return this;
    }

    MdmCmd setTo(int pTo) {
        mTo = pTo;
        return this;
    }

    MdmCmd setCmdType(int pCmdType) {
        mCmdType = pCmdType;
        return this;
    }

    MdmCmd setSessionId(@Nullable String sessionId) {
        this.mSessionId = TextUtils.isEmpty(sessionId) ? mSessionId : sessionId;
        return this;
    }

    MdmCmd setCmdName(String pCmdName) {
        mCmdName = pCmdName;
        return this;
    }

    MdmCmd setRetryInterval(long retryInterval) {
        mRetryInterval = retryInterval;
        return this;
    }

    MdmCmd setRetryNum(int retryNum) {
        mRetryNum = retryNum;
        return this;
    }

    MdmCmd setRetryTime(long retryTime) {
        mRetryTime = retryTime;
        return this;
    }


    public JSONObject getCmdJson() {
        return mCmdJson;
    }
    public int getTo() {
        return mTo;
    }
    public int getFrom() {
        return mFrom;
    }
    public int getCmdType() {
        return mCmdType;
    }
    public String getSessionId() {
        return mSessionId;
    }
    public String getCmdName() {
        return mCmdName;
    }
    public long getSendVersion() {
        return mSendVersion;
    }
    public MdmCmd setErrorCode(int errorCode) {
        this.mErrorCode = errorCode;
        return this;
    }
    public MdmCmd setMsgCode(int pMsgCode) {
        mMsgCode = pMsgCode;
        return this;
    }
    public MdmCmd setMsg(String pMsg) {
        mMsg = pMsg;
        return this;
    }
    public int getErrorCode() {
        return mErrorCode;
    }
    public String getMsg() {
        return mMsg == null || mMsg.isEmpty() ? "Null" : mMsg;
    }
    public int getMsgCode() {
        return mMsgCode;
    }
    public String getCmdBizName() {
//        return TextUtils.isEmpty(mCmdBizName) ? mCmdName : mCmdBizName;
        return mCmdName;
    }
    public long getStartTime() {
        return mStartTime;
    }
    public int getRetryNum() {
        return mRetryNum;
    }
    public long getRetryInterval() {
        return mRetryInterval;
    }
    public long getRetryTime() {
        return mRetryTime;
    }
    public MdmCmd setDelayTime(long pDelayTime) {
        mDelayTime = pDelayTime;
        return this;
    }
    public long getDelayTime() {
        return mDelayTime;
    }

    protected MdmCmd setCmdBizName(@StringRes int pCmdBizNameRes) {
//        mCmdBizName = mContext.getString(pCmdBizNameRes);
        return this;
    }


    @CallSuper
    public void release() {
        mIsReleased.set(true);
    }

    public boolean isReleased() {
        return mIsReleased.get();
    }

    public void execute() throws JSONException {
    }
}
