package com.nd.mdm.communicate;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.nd.android.adhoc.basic.common.util.AdhocDataCheckUtils;
import com.nd.android.adhoc.basic.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class PushRequestMaker {
    private static String TAG = "PushRequestMaker";

    public static String makeContentFormRequest(Request pRequest) {

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("action", getAction(pRequest));
            jsonObject.put("method", getMethod());
            jsonObject.put("header", getHeader(pRequest));
            jsonObject.put("content", getContent(pRequest));

            return jsonObject.toString();

        } catch (Exception e) {
            Logger.e(TAG, "make content json error: " + e);
        }

        return null;
    }

    @NonNull
    public static String getMethod() {
        return null;
    }

    @NonNull
    private static String getAction(@NonNull Request pRequest) {

        HttpUrl httpUrl = pRequest.url();

        // url 后面的路径
        List<String> pathSegments = httpUrl.pathSegments();
        if (AdhocDataCheckUtils.isCollectionEmpty(pathSegments)) {
            return "";
        }

        StringBuilder action = new StringBuilder();
        for (String pathSegment : pathSegments) {
            action.append("/").append(pathSegment);
        }
        return action.toString();
    }

    @NonNull
    private static String getHeader(@NonNull Request pRequest) {

        //拼接 headers
        Set<String> headersNames = pRequest.headers().names();

        try {
            JSONObject headers = new JSONObject();
            if (!AdhocDataCheckUtils.isCollectionEmpty(headersNames)) {
                for (String headersName : headersNames) {
                    if (TextUtils.isEmpty(headersName)) {
                        continue;
                    }
                    headers.put(headersName, pRequest.headers().get(headersName));
                }
            }

            return headers.toString();
        } catch (JSONException e) {
            Logger.e(TAG, "getHeader error: " + e);
        }
        return "";
    }

    @NonNull
    protected static String getContent(@NonNull Request pRequest) {
        return null;
    }
}
