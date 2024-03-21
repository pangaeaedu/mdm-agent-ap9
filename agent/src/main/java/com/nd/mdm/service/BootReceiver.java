package com.nd.mdm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // 在这里启动你的后台服务
            Intent serviceIntent = new Intent(context, MdmService.class);
            context.startService(serviceIntent);
        }
    }
}
