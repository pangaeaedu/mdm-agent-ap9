package com.nd.mdm.agent;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.mdm.communicate.AdhocPushRequestOperator;
import com.nd.mdm.communicate.PushModule;
import com.nd.mdm.device_control.Ap9Control_Info;
import com.nd.mdm.device_control.Ap9Control_Power;
import com.nd.mdm.device_control.Ap9Control_Screen;
import com.nd.mdm.device_control.Ap9Control_hardware;

import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Request;
import okhttp3.Response;
import rx.Subscriber;

public class MainActivity extends Activity {
    private PushModule mPushModule = null;
    Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Request request = null;
        AtomicReference<String> msgContent = null;
        setContentView(R.layout.activity_main);

        initPermission();

        initUI();
    }

    private void initUI() {
        EditText edtPanelInfo = findViewById(R.id.edt_panel_info);
        edtPanelInfo.setText(Ap9Control_Info.getDeviceInfo());

        Button btnConnectPush = findViewById(R.id.btn_connectPush);
        btnConnectPush.setOnClickListener(v -> {

            //Before new Push Module, we need to start Push Channel first.

            mPushModule = new PushModule();
            mPushModule.start();
            mPushModule.fireConnectatusEvent();
            Toast.makeText(MainActivity.this, "Push Channel init!", Toast.LENGTH_SHORT).show();
        });

        Button btnSendHello = findViewById(R.id.btn_sendHello);
        btnSendHello.setOnClickListener(v -> {
//            mPushModule = new PushModule();
//            msgContent.set(PushRequestMaker.makeContentFormRequest(request));
//            mPushModule.sendUpStreamMsg("TestAP9", null, 20 * 1000, "", msgContent.get());
            AdhocPushRequestOperator.doRequest(null, 20 * 1000, "", "zhouyu").subscribe(new Subscriber<Response>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Response response) {

                }
            });

            //mPushModule.sendUpStreamMsg("sync_res", null, 20 / 1000, "contentType", content);


        });

        Button btnEnroll = findViewById(R.id.btn_enroll);
        btnEnroll.setOnClickListener(v -> {
            mPushModule.sendUpStreamMsg("sync_res_ap9", null, 20, "", Ap9Control_hardware.makeContentHardwareInfo());
            Toast.makeText(MainActivity.this, "Enroll OK!", Toast.LENGTH_SHORT).show();
        });

        Button btnScreenSetBrightLight = findViewById(R.id.btn_power_reboot);
        btnScreenSetBrightLight.setOnClickListener(v -> {
            Ap9Control_Power.reboot();
        });

        Button btnScreenLock = findViewById(R.id.btn_screen_lock);
        btnScreenLock.setOnClickListener(v -> {
            Ap9Control_Screen.lockScreen();
        });

        Button btnSetBrightLight = findViewById(R.id.btn_screen_setBright_light);
        btnSetBrightLight.setOnClickListener(v -> {
            Ap9Control_Screen.setBrightnessLight();
        });

        Button btnSetBrightDark = findViewById(R.id.btn_screen_setBright_dark);
        btnSetBrightDark.setOnClickListener(v -> {
            Ap9Control_Screen.setBrightnessDark();
        });

        Button btnNotifySystem = findViewById(R.id.btn_notify_system);
        btnNotifySystem.setOnClickListener(v -> {
            Ap9Control_Info.Notify(mContext);
        });

        Button btnNotifyAlert = findViewById(R.id.btn_notify_alert);
        btnNotifyAlert.setOnClickListener(v -> {
            Ap9Control_Info.Alert(mContext);
        });

        Button btnOpenWifi = findViewById(R.id.btn_open_wifi);
        btnOpenWifi.setOnClickListener(v -> {
            Ap9Control_hardware.OpenWifi(mContext);
        });

    }

    private void initPermission() {
        //获取系统设置写入权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.System.canWrite(AdhocBasicConfig.getInstance().getAppContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + AdhocBasicConfig.getInstance().getAppContext().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            AdhocBasicConfig.getInstance().getAppContext().startActivity(intent);
        }

        // 获取device admin权限
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        // 检查是否有锁屏权限
        ComponentName adminComponent = new ComponentName(this, DeviceAdminReceiver.class);
        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            // 请求锁屏权限
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "请授予锁屏权限");
            startActivity(intent);
        }
    }
}