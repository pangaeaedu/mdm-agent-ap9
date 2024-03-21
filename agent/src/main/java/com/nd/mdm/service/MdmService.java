package com.nd.mdm.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.nd.mdm.communicate.PushModule;
import com.nd.mdm.device_control.Ap9Control_hardware;

public class MdmService extends Service {
    private static final String TAG = "MdmService";
    private static PushModule mPushModel = new PushModule();

    public MdmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mPushModel.start();

        mPushModel.sendUpStreamMsg("sync_res_ap9", null, 20, "", Ap9Control_hardware.makeContentHardwareInfo());

        return START_STICKY;
    }
}