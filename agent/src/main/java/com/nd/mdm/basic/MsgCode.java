package com.nd.mdm.basic;

public class MsgCode {
    public static final int ERROR_PERMISSION_DENIED = 10016;
    public static final int ERROR_NONE = 0;                                 // 没有错误
    public static final int ERROR_UNKNOWN = 10000;                          // 未知错误
    public static final int ERROR_PARAMETER = 10001;                        // 传入参数有误，执行失败
    public static final int ERROR_JSON_INVALID = 10002;                     // JSON格式不合法，解析失败
    public static final int ERROR_COMMAND_UNSUPPORT = 10003;                // 本装置不支持此指令
    public static final int ERROR_COMMAND_MODULE_UNINITIALIZATION = 10004;  // 初始化功能模块失败
    public static final int ERROR_COMMAND_EXECUTE_FAIL = 10005;             // 指令执行失败
    public static final int ERROR_FILE_NOT_EXIST = 10006;                   // 文件不存在
    public static final int ERROR_FILE_DELETE_FAIL = 10007;                 // 文件删除失败
    public static final int ERROR_DOWNLOAD_FAIL = 10008;                    // 下载失败
    public static final int ERROR_DOWNLOAD_MD5_FAIL = 10009;                // 下载失败，MD5校验失败
    public static final int ERROR_POLICYSET_NOT_EXIST = 10010;              // 策略集未下发
    public static final int ERROR_SCREENSHOT = 10011;                       // 截屏失败
    public static final int ERROR_WIFI_SSID_NOT_EXIT = 10012;               // 装置附近没有路由的SSID和命令指定的相同
    public static final int ERROR_SDCARD_SPACE_NOT_ENOUGH = 10013;          // SDCard空间不足
    public static final int ERROR_MEDIA_ERROR = 10014;                      // 播放失败
    public static final int ERROR_CAMERA_OPEN_FAILED = 10015;               // 打开摄像头失败
    public static final int ERROR_MEDIA_LOAD_ERROR = 10016;                 // 加载失败
    public static final int ERROR_DECOMPRESS_ERROR = 10017;                 // 解压失败
    public static final int ERROR_TEMPLATE_NOTEXIST = 10018;                // 模板不存在（分众传媒HTML模板）

    public static final int ERROR_SYSTEM_SPACE_NOT_ENOUGH = 10033;          // 系统空间不足
    public static final int ERROR_STORAGE_SPACE_NOT_ENOUGH = 10034;          // 设备存储空间不足
    public static final int ERROR_APP_NOT_EXIST = 10035;                // 应用不存在
    public static final int ERROR_DOWNLOAD_FILE_NOT_EXIST = 10036;                // 下载文件失败，对应文件不存在
    public static final int ERROR_VIOLATION_OF_POLICY = 10037;                // 违反策略限制

    public static final int ERROR_COMMAND_EXPIRED = 10038;                  // 指令过期
    public static final int ERROR_COMMAND_WITHDRAWN = 10039;                  // 指令已被撤回

    public static final int STATUS_MEDIA_READY = 20001;                     // 开始播放
    public static final int STATUS_MEDIA_COMPLETE = 20001;                  // 播放结束
    public static final int STATUS_MEDIA_PAUSE = 20003;                     // 暂停
    public static final int STATUS_MEDIA_FAILED = 20004;                    // 异常
    public static final int STATUS_MEDIA_EXIT = 20005;                      // 退出
    public static final int EXECUTING=999;//执行中
}
