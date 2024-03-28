package com.nd.mdm.communicate;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nd.android.adhoc.basic.common.exception.AdhocException;
import com.nd.android.adhoc.basic.common.util.AdhocDataCheckUtils;
import com.nd.android.adhoc.basic.frame.api.initialization.AdhocAppInitManager;
import com.nd.android.adhoc.basic.frame.api.initialization.AdhocBlockingException;
import com.nd.android.adhoc.basic.frame.api.initialization.IAdhocInitStatusListener;
import com.nd.android.adhoc.basic.log.Logger;
import com.nd.android.adhoc.basic.util.string.AdhocMD5Util;
import com.nd.mdm.command.CommandFromTo;
import com.nd.mdm.command.MdmCmdManager;
import com.nd.sdp.android.serviceloader.annotation.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Service(PushDataOperator.class)
public class PushDataOperator {
    private static final String TAG = "PushDataOperator";

    private final List<Pair<String, Map<String, String>>> mCacheMsg = new CopyOnWriteArrayList<>();

    private final AtomicBoolean mAllowSend = new AtomicBoolean(false);

    private IAdhocInitStatusListener mAdhocInitStatusListener = new IAdhocInitStatusListener() {
        @Override
        public void onIniting(@NonNull String pInitProgressMsg) {
            mAllowSend.set(false);
        }

        @Override
        public void onInitSuccess() {
            AdhocAppInitManager.getInstance().getInitOperator().removeInitStatusListener(this);
            mAllowSend.set(true);
            if (mCacheMsg.isEmpty()) {
                return;
            }

            for (Pair<String, Map<String, String>> item : mCacheMsg) {

                Log.e(TAG, "init success, post all hold msg");
                onPushDataArrived(item.first, item.second);
            }

            mCacheMsg.clear();
        }

        @Override
        public boolean onInitFailed(@NonNull AdhocException pException) {
            // 阻塞造成的，不处理
            if (pException instanceof AdhocBlockingException) {
                return false;
            }
            AdhocAppInitManager.getInstance().getInitOperator().removeInitStatusListener(this);
            return false;
        }
    };

    public PushDataOperator() {
        super();

        AdhocAppInitManager.getInstance().getInitOperator().addInitStatusListener(mAdhocInitStatusListener);
    }

    public boolean isPushMsgTypeMatche(int pPushMsgType) {
        return pPushMsgType == 0;
    }

    public void onPushDataArrived(@NonNull String pData, @Nullable Map<String, String> pExtraInfos) {
        if (TextUtils.isEmpty(pData)) {
            Logger.w(TAG, "onPushDataArrived: Cmd message bytes is null");
            return;
        }

        if (!mAllowSend.get()) {
            Logger.w(TAG, "mAllowSend is false!");
            mCacheMsg.add(new Pair<>(pData, pExtraInfos));
            return;
        }

        boolean checkHash = checkHash(pData, pExtraInfos);
        Logger.d(TAG, "checkHash result = " + checkHash);

        // hash 校验不通过，证明数据不正确，可能被篡改，也可能数据丢失
        if (!checkHash) {
            return;
        }

        Logger.i(TAG, "onPushDataArrived ");
        MdmCmdManager.doCmdReceived(pData, CommandFromTo.MDM_CMD_DRM, CommandFromTo.MDM_CMD_DRM);

//        AdhocProcessedCmdManager.getInstance().onCmdReceived(pData);
    }

    private boolean checkHash(@NonNull String pData, @Nullable Map<String, String> pExtraInfos) {
        Logger.d(TAG, "checkHash ");
        if (!AdhocDataCheckUtils.isMapEmpty(pExtraInfos)
                && pExtraInfos.containsKey("cert")) {
            // 不为空，判断是否需要校验数据
            String dataHash = null;
            try {
                dataHash = AdhocMD5Util.sha1Encode(pData+"ʐ%վ]ȩĿʮբ8ڴzϊm¢dl");
            } catch (Exception e) {
                e.printStackTrace();
            }

            dataHash = dataHash == null ? "" : dataHash;

            String cert = pExtraInfos.get("cert");
            if (!dataHash.equals(cert)) {
                try {
//                    ICmdContent_MDM cmdContent = MdmCmdHelper.commandParsing(pData, AdhocCmdFromTo.MDM_CMD_DRM, AdhocCmdFromTo.MDM_CMD_DRM, AdhocCmdType.CMD_TYPE_STATUS);
//                    Logger.d(TAG, "checkHash, sessionId: " + cmdContent.getSessionId() + ", cmdName = " + cmdContent.getCmdName());
//
//                    String cmdName = cmdContent.getCmdName();
//                    if (TextUtils.isEmpty(cmdName)) {
//                        cmdName = "unknow_cmd";
//                    }
//
//                    JSONObject errorData = new JSONObject();
//                    errorData.put("result", pData);
//
//                    MdmResponseHelper.createResponseBase(cmdName, "", cmdContent.getSessionId(), cmdContent.getTo(), System.currentTimeMillis())
//                            .setSendVersion(cmdContent.getSendVersion())
//                            .setErrorCode(ErrorCode.FAILED)
//                            .setMsgCode(10099)
//                            .setMsg("The original data of the command has been tampered")
//                            .setJsonData(errorData)
//                            .post();
                } catch (Exception e) {
                    Logger.e(TAG, "checkHash, parsing cmd content error, result is not feedback: " + e);
                }
                return false;
            }
        }

        return true;
    }
}
