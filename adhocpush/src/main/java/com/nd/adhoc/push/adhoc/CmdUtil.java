package com.nd.adhoc.push.adhoc;

import android.content.Context;
import android.os.Bundle;

import com.nd.android.adhoc.basic.log.Logger;
import com.nd.android.adhoc.basic.util.storage.AdhocFileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmdUtil {
    private static final String TAG = "CmdUtil";


    public static final int INSTALL_SUCCESS = 0;
    public static final int INSTALL_FAILED = 1;
    public static final int INSTALL_NO_RESULT = 2;
    public static final int UNINSTALL_SUCCESS = 0;
    public static final int UNINSTALL_FAILED = 1;
    public static final int UNINSTALL_NO_RESULT = 2;

    public static String runCmd(String cmd) {
        return runCmd(cmd.split("\\s+"));
    }

    public static String runCmd(String[] args) {
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
//            Log.d(TAG, result);
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                Logger.e(TAG, e.toString());
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

    /**
     * system app only,need system signed
     *
     * @param context
     * @param path
     * @return
     */
    private static boolean cmdInstall(Context context, String path) {
        Logger.d(TAG, "install apk:" + path);
        File installFile = new File(path);
        if (installFile.exists()) {
            String[] args = {"pm", "install", "-r", "\"" + path + "\""};
            String result = CmdUtil.runCmd(args);
            if (result.contains("Success")) {
                Logger.d(TAG, "install path:" + path + " success");
                return true;
            } else {
                Logger.e(TAG,  "install path:" + path + " failed,result:" + result);
            }
        } else {
            Logger.e(TAG,  "install file not found:" + path);
        }
        return false;
    }

//    public static void install(Context context, String path) {
//        try {
//            File file = new File(path);
//            if (file.exists()) {
//                new DialogEvent(DialogEvent.HIDE_DIALOG).show();
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//            }
//        } catch (Exception e) {
//            SDKLogUtil.e(e.toString());
//        }
//    }
//
//    public static void uninstall(Context context, String packageName) {
//        new DialogEvent(DialogEvent.HIDE_DIALOG).show();
//        Uri uri = Uri.parse("package:" + packageName);
//        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
//    }

    private static boolean cmdUninstall(Context context, String packageName) {
        String[] args = {"pm", "uninstall", packageName};
        String result = runCmd(args);
        if (result.contains("Success")) {
            Logger.d(TAG, "uninstall package " + packageName + " success");
            return true;
        } else {
            Logger.d(TAG, "uninstall package " + packageName + " failed");
            return false;
        }
    }

    /**
     * <p>Description: 屏幕截屏             </p>
     * <p>Create Time: 2015年12月3日   </p>
     * <p>Create author: hyq   </p>
     *
     * @param context   上下文
     * @param imagePath 图片存放路径
     */
    public static boolean takeScreenShot(Context context, String imagePath) {

        // Runtime.getRuntime().exec("screencap -p " + imagePath);
        String[] args = {"screencap", "-p", imagePath};
        String result = CmdUtil.runCmd(args);
        if (AdhocFileUtil.isFileExit(imagePath)) {
            return true;
        } else {
            return false;
        }
    }

    public static Bundle ping(String target, int count, int size) {
        Bundle bundle = new Bundle();
        String res = runCmd(new String[]{"ping", "-c", String.valueOf(count), "-s", String.valueOf(size), target});
        float maxTime = Float.MIN_VALUE;
        float minTime = Float.MAX_VALUE;
        float totalTime = 0;
        Pattern pattern = Pattern.compile("\\d+ bytes from [\\w\\.]+: icmp_seq=(\\d+) ttl=(\\d+) time=([\\d\\.]+) ms");
        Matcher matcher = pattern.matcher(res);
        int index = 0;
        while (matcher.find(index)) {
            float cost = Float.valueOf(matcher.group(3));
            minTime = minTime < cost ? minTime : cost;
            maxTime = maxTime > cost ? maxTime : cost;
            totalTime += cost;
            index = matcher.end() + 1;
        }
        pattern = Pattern.compile("(\\d+) packets transmitted, (\\d+) received, ([\\d\\.]+)% packet loss, time (\\d+)ms");
        matcher = pattern.matcher(res);
        if (matcher.find()) {
            bundle.putInt("receive", Integer.valueOf(matcher.group(2)));
            bundle.putFloat("average", totalTime / Integer.valueOf(matcher.group(2)));
            bundle.putFloat("loss", Float.valueOf(matcher.group(3)));

        }
        if (minTime != Integer.MAX_VALUE && maxTime != Integer.MIN_VALUE) {
            bundle.putFloat("min", minTime);
            bundle.putFloat("max", maxTime);
        }
        return bundle;
    }
}
