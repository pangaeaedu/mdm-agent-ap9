package com.nd.mdm.device_control;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.mdm.agent.Ap9DeviceAdminReceiver;

public class Ap9Control_Power {
    static final Context context = AdhocBasicConfig.getInstance().getAppContext();


    public boolean shutdown(long seconds) throws Exception {
        return true;
    }

    public static void reboot() {
        // 重启需要使用device owner: adb shell dpm set-device-owner com.nd.mdm.agent/com.nd.mdm.agent.Ap9DeviceAdminReceiver
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(context, Ap9DeviceAdminReceiver.class);
        boolean isAdminActive = devicePolicyManager.isAdminActive(adminComponent);
        if (isAdminActive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                devicePolicyManager.reboot(adminComponent);
            }
        } else {
            // 没有系统管理员权限，需要请求权限
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "请授予系统管理员权限以执行关机操作");
            context.startActivity(intent);
        }
    }
}
