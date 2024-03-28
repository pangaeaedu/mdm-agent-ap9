package com.nd.mdm.command;

import android.content.Context;

import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.mdm.device_control.Ap9Control_Info;

import org.json.JSONException;
import org.json.JSONObject;

public class CmdSendMsg extends MdmCmd {
    private JSONObject jsonData;
    private String title;
    private String content;
    private String ext_sender_id;
    private String ext_sender_name;

    static final Context context = AdhocBasicConfig.getInstance().getAppContext();

    public CmdSendMsg(Context context, JSONObject pCmdJson) throws JSONException {
        super(context, pCmdJson);
        jsonData = pCmdJson.getJSONObject("data");
    }

    @Override
    public void execute() throws JSONException {
        title = jsonData.getString("title");
        content = jsonData.getString("content");
        ext_sender_id = jsonData.getString("ext_sender_id");
        ext_sender_name = jsonData.getString("ext_sender_name");

        Ap9Control_Info.alert(context, title, content);
    }
}
