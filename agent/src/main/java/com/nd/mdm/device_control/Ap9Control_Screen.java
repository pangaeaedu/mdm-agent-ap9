package com.nd.mdm.device_control;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;

import com.nd.android.adhoc.basic.common.AdhocBasicConfig;

public class Ap9Control_Screen {
    static final Context context = AdhocBasicConfig.getInstance().getAppContext();

    public static void lockScreen() {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(context, DeviceAdminReceiver.class);
        // 锁定屏幕
        devicePolicyManager.lockNow();
    }

    public static void setBrightnessLight() {
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        // 设置屏幕亮度为指定值（0-255）
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 200);
    }
    public static void setBrightnessDark() {
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        // 设置屏幕亮度为指定值（0-255）
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 50);
    }
}
