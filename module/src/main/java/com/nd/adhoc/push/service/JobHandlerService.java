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
import android.os.Build;

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

    public JobHandlerService() {
        super();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        log.info("onStartJob");
//        || isServiceRunning(this, "com.ph.myservice.RemoteService") == false
//        if (!isServiceRunning(getApplicationContext(), "com.ph.myservice") || !isServiceRunning(getApplicationContext(), "com.ph.myservice:remote")) {
//            startService(new Intent(this, LocalService.class));
//            startService(new Intent(this, RemoteService.class));
//        }

       /* boolean serviceRunning = isServiceRunning(getApplicationContext(), "com.ph.myservice");
        System.out.println("进程一" + serviceRunning);

        boolean serviceRunning2 = isServiceRunning(getApplicationContext(), "com.ph.myservice:remote");
        System.out.println("进程二" + serviceRunning2);*/
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
//        if (!isServiceRunning(this, "com.ph.myservice.LocalService") || !isServiceRunning(this, "com.ph.myservice.RemoteService")) {
//            startService(new Intent(this, LocalService.class));
//            startService(new Intent(this, RemoteService.class));
//        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.info("onStartCommand");
//        startService(new Intent(this, LocalService.class));
//        startService(new Intent(this, RemoteService.class));

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

    // 服务是否运行
    public boolean isServiceRunning(Context context, String serviceName) {
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
