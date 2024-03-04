package com.nd.mdm.communicate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nd.android.adhoc.basic.common.util.AdhocDataCheckUtils;
import com.nd.android.adhoc.basic.log.Logger;
import com.nd.android.adhoc.basic.util.string.AdhocMD5Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class PushDataOperator {
    private static final String TAG = "PushDataOperator";

    boolean isPushMsgTypeMatche(int pPushMsgType) {
        return pPushMsgType == 1;
    }


    void onPushDataArrived(@NonNull String pData, @Nullable Map<String, String> pExtraInfos) {

        String message_id = "";
        try {
            JSONObject object = new JSONObject(pData);
            message_id = object.optString("message_id");
        } catch (JSONException e) {
            Logger.w(TAG, "get msgid error: " + e);
        }
        Logger.d(TAG, "onPushDataArrived, message_id = " + message_id);

        // 如果数据不对，就强制塞错误码进去
        if (!checkHash(pData, pExtraInfos)) {
            try {
                JSONObject jsonObject = new JSONObject(pData);
                jsonObject.put("code", 10099);
                jsonObject.put("message", "The original data of the data has been tampered");

                pData = jsonObject.toString();
            } catch (JSONException e) {
                Logger.w(TAG, "check data tampered error: " + e);
            }
        }

        AdhocPushRequestOperator.receiveFeedback(pData);
    }


    private boolean checkHash(@NonNull String pData, @Nullable Map<String, String> pExtraInfos) {
        if (!AdhocDataCheckUtils.isMapEmpty(pExtraInfos)
                && pExtraInfos.containsKey("cert")) {
            // 不为空，判断是否需要校验数据
            String dataHash = null;
            try {
                dataHash = AdhocMD5Util.sha1Encode(pData+"ʐ%վ]ȩĿʮբ8ڴzϊm¢dl");
            } catch (Exception e) {
                Logger.w(TAG, "check data error: " + e);
            }

            dataHash = dataHash == null ? "" : dataHash;

            String cert = pExtraInfos.get("cert");
            return dataHash.equals(cert);
        }

        return true;
    }
}
