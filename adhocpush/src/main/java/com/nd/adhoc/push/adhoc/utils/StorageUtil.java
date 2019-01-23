package com.nd.adhoc.push.adhoc.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.util.Locale;

/**
 * Created by XWQ on 2018/7/24 0024.
 */

public class StorageUtil {

    @SuppressLint("SdCardPath")
    private static final String ND3_SDCARD_PATH = "/sdcard";

    private static Boolean mSdCardExist = null;
    private static String mSdCardPath = null;

    /**
     * 获取 SD 卡根目录
     * ND3 设备：固定返回 /sdcard/，目的是为了兼容早期 ND3 ROM 下用 Environment.getExternalStorageDirectory() 获取路径存在 BUG
     *
     * @return SD 卡根目录
     */
    public static String getSdCardPath() {
        if (DeviceUtil.isND3Device()) {
            return ND3_SDCARD_PATH;
        }

        File sdDir;
        boolean sdCardExist = isSdCardExist();  //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
            return sdDir.toString();
        } else {
            return "";
        }
    }

    /**
     * 判断 SD 卡是否存在
     * @return true：是，false：否
     */
    public static boolean isSdCardExist() {
        if (mSdCardExist == null) {

            if (DeviceUtil.isND3Device()) {
                return mSdCardExist = new File(ND3_SDCARD_PATH).exists();
            }

            mSdCardExist = Environment.getExternalStorageState().equals(Environment
                    .MEDIA_MOUNTED);
        }
        return mSdCardExist;
    }

    /**
     * 获得SD卡中应用程序数据目录
     * @param pContext 上下文
     * @return SD 卡缓存路径
     */
    private static String getRealSdCardCacheDirPath(Context pContext){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
            if(pContext.getExternalCacheDir() == null){
                return "";
            }
            return makesureFileSepInTheEnd(pContext.getExternalCacheDir().getPath());
        } else {
            if(!isSdCardExist()){
                return "";
            }

            String sdcardPath = DeviceUtil.isND3Device() ? ND3_SDCARD_PATH : Environment.getExternalStorageDirectory().getPath();

            return String.format(Locale.getDefault(),
                    sdcardPath
                            + "/Android/data/%s/cache/", pContext.getPackageName());
        }
    }

    /**
     * 获得SD卡中应用程序数据目录
     * @param pContext 上下文
     * @return string SD卡中应用程序数据目录
     * by  : cb
     * date  : 2015-02-26 03-32-14
     */
    public static String getSDCardCacheDir(Context pContext) {
        if(TextUtils.isEmpty(mSdCardPath)){
            mSdCardPath = getRealSdCardCacheDirPath(pContext);
        }
        return mSdCardPath;
    }

    /**
     * @param pStrDir 文件路径
     * @return string 末尾带斜杠的文件路径
     * @brief 确保末尾的斜杠
     */
    public static String makesureFileSepInTheEnd(String pStrDir) {
        if (!TextUtils.isEmpty(pStrDir)) {
            //确保以斜杠结尾
            if (!pStrDir.endsWith(File.separator)) {
                pStrDir += File.separator;
            }
        }
        return pStrDir;
    }
}
