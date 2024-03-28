package com.nd.mdm.device_control;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.nd.adhoc.push.adhoc.utils.DeviceUtil;
import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.mdm.agent.R;

public class Ap9Control_Info {
    static final String topicAP9 = "activate_ap9";
    static final Context context = AdhocBasicConfig.getInstance().getAppContext();
    static String deviceUUID = DeviceUtil.getDeviceUUID(context, false);
    static String mManufactor = DeviceUtil.getManufactorer();
    static String mAndroidId = DeviceUtil.getAndroidId(context);
    static String deviceName = Settings.Global.getString(context.getContentResolver(), Settings.Global.DEVICE_NAME);

    public static String getDeviceInfo() {
        return "deviceName: " + deviceName + "\n" +
                "deviceUUID: " + deviceUUID + "\n" +
                "Manufactor: " + mManufactor + "\n" +
                "AndroidId: " + mAndroidId + "\n";
    }

    public static void notify(Context mContext) {
        // 创建通知渠道（仅需要在 Android 8.0（API 级别 26）及更高版本上创建）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // 创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, "channel_id")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("系统消息")
                .setContentText("这是一条系统消息")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

       // 发送通知
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(0, builder.build());
    }

    public static void alert(Context mContext, String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String msgTitle = title;
        String msgContent = content;

        builder.setTitle(msgTitle)
                .setMessage(msgContent)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 点击确定按钮后的操作
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 点击取消按钮后的操作
                    }
                });
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
