package com.nd.adhoc.push.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import com.nd.adhoc.push.PushSdk;
import com.nd.sdp.adhoc.push.IDaemonService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by XWQ on 2017/8/31 0031.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobHandlerService extends JobService {
    private static Logger log = LoggerFactory.getLogger(JobHandlerService.class.getSimpleName());
    //每隔5秒运行一次
    private final static int Period_Time = 5000;
    private JobScheduler mJobScheduler;
    private IDaemonService mDaemonService;

    private ServiceConnection mDaemonServiceConnection  = new ServiceConnection() {
        /**
         * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
         * 通过这个IBinder对象，实现宿主和Service的交互。
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            log.info("PushService mDaemonServiceConnection onServiceConnected()");
            mDaemonService = IDaemonService.Stub.asInterface(binder);
            if (mDaemonService != null) {
                try {
                    mDaemonService.startMonitorPushService();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 当取消绑定的时候被回调。但正常情况下是不被调用的，它的调用时机是当Service服务被意外销毁时，
         * 例如内存的资源不足时这个方法才被自动调用。
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            log.info("PushService mDaemonServiceConnection onServiceDisconnected()");
            reStartService();
        }
    };

    public JobHandlerService() {
        super();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        log.info("onStartJob");
        reStartService();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        log.info("onStopJob");
        reStartService();
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.info("onStartCommand");
        PushSdk.getInstance().startPushService(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(startId++,
                    new ComponentName(getPackageName(), JobHandlerService.class.getName()));

            builder.setPeriodic(Period_Time);
            builder.setRequiresCharging(true);
            //设置设备重启后，是否重新执行任务
            builder.setPersisted(true);
            builder.setRequiresDeviceIdle(true);

            if (mJobScheduler.schedule(builder.build()) <= 0) {
                log.info("JobHandlerService work Success.");
            } else {
                log.info("JobHandlerService work Fail.");
            }
        }
        // 如果Service被终止
        // 当资源允许情况下，重启service
        return START_STICKY;
    }

    private void reStartService() {
        log.info("startDaemonService()");
        if (!isServiceRunning(getApplicationContext(), "com.nd.adhoc.push.service.PushService")) {
            startService(new Intent(getApplicationContext(), PushService.class));
        } else if (!isServiceRunning(getApplicationContext(), "com.nd.adhoc.push.service.DaemonService")) {
            Intent intentDaemon = new Intent(getApplicationContext(), DaemonService.class);
            ComponentName componentName = startService(intentDaemon);
            log.info("startDaemonService() componentName = " + componentName);
            boolean ret = bindService(intentDaemon, mDaemonServiceConnection, Context.BIND_IMPORTANT);
            log.info("bindDaemonService() ret = " + ret);
        }
    }

    // 服务是否运行
    private boolean isServiceRunning(Context context, String serviceName) {
        boolean isRunning = false;
        ActivityManager am = (ActivityManager) this
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lists = am.getRunningAppProcesses();


        for (ActivityManager.RunningAppProcessInfo info : lists) {// 获取运行服务再启动
            System.out.println(info.processName);
            if (info.processName.equals(serviceName)) {
                isRunning = true;
            }
        }
        return isRunning;

    }

}
