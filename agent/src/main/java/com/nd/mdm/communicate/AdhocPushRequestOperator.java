package com.nd.mdm.communicate;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.nd.android.adhoc.basic.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class AdhocPushRequestOperator {

    private static final String TAG = "AdhocPushRequestOperator";

    private static final int DEFAULT_ERROR_CODE = -9999;

    private static PublishSubject<String> mPushFeedbackSub = PublishSubject.create();
    private static PushModule mPushModel = new PushModule();

    /**
     * 执行请求
     *
     * @param msgid          消息ID，如果不传，默认随机生成 UUID
     * @param ttlMillSeconds 超时时间，必填
     * @param contentType    内容类型，选填
     * @param content        请求内容，必填
     * @return Observable<Response>
     */
    public static Observable<Response> doRequest(String msgid, final long ttlMillSeconds, final String contentType, @NonNull final String content) {
        if (TextUtils.isEmpty(msgid)) {
            msgid = UUID.randomUUID().toString();
        }

        Logger.i(TAG, "doRequest: msgid = " + msgid);
        Logger.d(TAG, "doRequest: msgid = " + msgid + ", content = " + content);

        final String finalMsgid = msgid;
        return mPushFeedbackSub.asObservable()
                .onBackpressureBuffer()
                .delay(500, TimeUnit.MILLISECONDS)
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String result) {

                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            String resultMsgId = jsonObject.optString("message_id");
                            // message_id 在 请求的列表中，才返回 true，继续执行，并且移除自身

                            if (finalMsgid.equals(resultMsgId)) {
                                return true;
                            }

                        } catch (JSONException e) {
                            Logger.w(TAG, "doRequest, on filter error: " + e);
                        }

                        return false;
                    }
                })
//                .map(new Func1<String, String>() {
//                    @Override
//                    public String call(String result) {
//                        // 内容为空，直接过滤掉
//                        if (TextUtils.isEmpty(result)) {
//                            return null;
//                        }
//
//                        try {
//                            JSONObject jsonObject = new JSONObject(result);
//                            String resultMsgId = jsonObject.optString("message_id");
//                            // message_id 在 请求的列表中，才返回 true，继续执行，并且移除自身
//
//                            if (finalMsgid.equals(resultMsgId)) {
//                                return result;
//                            }
//
//                        } catch (JSONException e) {
//                            Logger.w(TAG, "doRequest, on filter error: " + e);
//                        }
//
//                        return null;
//                    }
//                })
//                // 规定时间内还没有收到 有效的请求，就按照超时处理
                .timeout(ttlMillSeconds, TimeUnit.MILLISECONDS)
                // 这里把返回的结果转为 Response
                .map(new Func1<String, Response>() {
                    @Override
                    public Response call(String result) {

                        String resultContent = "";
                        String message;
                        int code;

                        try {

                            if (TextUtils.isEmpty(result)) {
                                code = -9999;
                                message = "result is null";
                            } else {
                                JSONObject jsonObject = new JSONObject(result);
                                resultContent = jsonObject.optString("content");
                                code = jsonObject.optInt("code", DEFAULT_ERROR_CODE);
                                message = jsonObject.optString("message");
                                if (DEFAULT_ERROR_CODE == code && TextUtils.isEmpty(message)) {
                                    message = "unknow result message...";
                                }
                            }

                            Logger.i(TAG, "message_id = " + finalMsgid + ", code = " + code + ", message = " + message);
                            Logger.d(TAG, "message_id = " + finalMsgid + ", code = " + code + ", message = " + message + ", resultContent = " + result);

                            Response.Builder builder = new Response.Builder();
                            builder.body(ResponseBody.create(MediaType.parse("application/json; charset=UTF-8"), resultContent))
                                    .code(code)
                                    .message(message)
                                    .protocol(Protocol.HTTP_1_1)
                                    .request(new Request.Builder().url("http://localhost/").build());

                            return builder.build();
                        } catch (Exception e) {
                            Logger.e(TAG, "doRequest error, parsing result error: " + e);

                            Response.Builder builder = new Response.Builder();
                            builder.body(ResponseBody.create(MediaType.parse("application/json; charset=UTF-8"), result))
                                    .code(DEFAULT_ERROR_CODE)
                                    .message("parsing result error: " + e)
                                    .protocol(Protocol.HTTP_1_1)
                                    .request(new Request.Builder().url("http://localhost/").build());

                            return builder.build();

                        }
                    }
                })
                // 订阅之后再发起请求，以免 先发起再订阅，会丢失返回结果
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mPushModel.sendUpStreamMsg("sync_res_ap9", finalMsgid, ttlMillSeconds / 1000, contentType, content);
                    }
                });
    }


    static void receiveFeedback(String pContent) {
        Logger.i(TAG, "receiveFeedback");

        if (TextUtils.isEmpty(pContent)) {
            Logger.w(TAG, "receiveFeedback, but content is empty");
            return;
        }
        mPushFeedbackSub.onNext(pContent);
    }
}
