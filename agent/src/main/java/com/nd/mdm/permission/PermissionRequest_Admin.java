package com.nd.mdm.permission;


import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.android.adhoc.basic.frame.api.permission.AdhocPermissionRequestAbs;
import com.nd.android.adhoc.basic.frame.api.permission.PermissionChecker;
import com.nd.android.adhoc.basic.ui.activity.AdhocRequestActivity;
import com.nd.android.mdm.basic.deviceactive.DeviceActivatedReciver;


//@Service(AdhocPermissionRequestAbs.class)
public class PermissionRequest_Admin extends AdhocPermissionRequestAbs {

    private static final String TAG = "PermAdmin";
    @Override
    public String getPermission() {
        return DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN;
    }

    @NonNull
    @Override
    public String geManifestPermission() {
        return "";
    }


    @Override
    public void doPermissionRequest(@NonNull final IAdhocPermissionRequestCallback pCallback) {
        Context context=AdhocBasicConfig.getInstance().getAppContext();
//        DevicePolicyManager devicePolicyManager=(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        //DeviceReceiver 继承自 DeviceAdminReceiver
        ComponentName componentName=new ComponentName(context, DeviceActivatedReciver.class);


        Intent intent = new Intent(
                DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                componentName);
        AdhocRequestActivity.startRequest(AdhocBasicConfig.getInstance().getAppContext(), intent, new AdhocRequestActivity.IResultCallback() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onCallback(int requestCode, int resultCode, Intent intent) {
                pCallback.onResult(checkGranted());
            }
        }, 0);
    }

    @Override
    public boolean checkGranted() {

        // 统一通过这个判断来执行，避免耦合
        if (PermissionChecker.isGranted(getPermission())) {
            return true;
        }
        Context context=AdhocBasicConfig.getInstance().getAppContext();

        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            boolean removable = appInfo.metaData.getBoolean("REMOVABLE_APP");
            if (removable) {
                Log.i(TAG, "checkGranted: this is a removable app");
                return true;
            }
        } catch (Exception pE) {
            pE.printStackTrace();
        }

        DevicePolicyManager devicePolicyManager=(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return devicePolicyManager.isAdminActive(new ComponentName(context, com.nd.android.mdm.basic.deviceactive.DeviceActivatedReciver.class));
    }

}

