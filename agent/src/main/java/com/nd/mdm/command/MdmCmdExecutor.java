package com.nd.mdm.command;

import androidx.annotation.NonNull;

import com.nd.android.adhoc.basic.common.exception.AdhocException;

import org.json.JSONException;

public class MdmCmdExecutor {
    public void executeCmd(@NonNull MdmCmd pCmd) throws AdhocException, JSONException {
        pCmd.execute();
    }
}
