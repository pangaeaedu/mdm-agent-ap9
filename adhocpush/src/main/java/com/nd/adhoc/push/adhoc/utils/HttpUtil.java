package com.nd.adhoc.push.adhoc.utils;

import com.nd.android.adhoc.basic.log.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by yaoyue1019 on 8-19.
 */

public class HttpUtil {
    private static final String TAG = "HttpUtil";

    public static String post(String url, String content, String pPushID) {
        try {
            Logger.d(TAG, String.format("post json to %s %s", url, content));
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(content, HTTP.UTF_8);
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            httpPost.addHeader("Authorization", MdmEncodeUtil.encode(pPushID));
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity());
                Logger.d(TAG, result);
//                EventBus.getDefault().post(new PushDrmsLogEvent(System.currentTimeMillis(), true));
                return result;
            } else {
                Logger.e(TAG, "receive error code:" + response.getStatusLine().getStatusCode());
//                EventBus.getDefault().post(new PushDrmsLogEvent(System.currentTimeMillis(), false));
                return null;
            }
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
            return null;
        }
    }


    public static String get(String url, Map<String, Object> params) {
        String ret = null;
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
            for (String key : params.keySet()) {
                pairs.add(new BasicNameValuePair(key, params.get(key).toString()));
            }
            try {
                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs), HTTP.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ret = get(url);
        return ret;
    }

    private static String get(String uri) {
        Logger.d(TAG, "get json from : " + uri);
        String result = null;
        try {
            HttpGet httpGet = new HttpGet(uri);
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                httpGet.abort();
                Logger.e(TAG, "HttpClient, error status code :" + statusCode);
            } else {
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    result = EntityUtils.toString(entity, HTTP.UTF_8);
                    return result;
                }
            }
        } catch (IOException e) {
            Logger.e(TAG, "request failed : " + e.toString());
        }
        return result;
    }
}
