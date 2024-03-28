package com.nd.mdm.command;

import androidx.annotation.NonNull;

import com.nd.android.adhoc.basic.common.exception.AdhocException;

import org.json.JSONException;

public class MdmCmdOperator {
    private MdmCmdCreator mCmdCreator;
    private MdmCmdExecutor mCmdExecutor;

    public MdmCmdOperator(@NonNull MdmCmdCreator pCmdCreator,
                           @NonNull MdmCmdExecutor pCmdExecutor) {
        mCmdCreator = pCmdCreator;
        mCmdExecutor = pCmdExecutor;
    }

    public boolean operate(@NonNull final MdmCmdContent pCmdContent) throws AdhocException {

        MdmCmd cmd = mCmdCreator.createCmd(pCmdContent);


        if (cmd != null) {
            try {
                mCmdExecutor.executeCmd(cmd);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
