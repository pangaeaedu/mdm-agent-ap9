package com.nd.mdm.command;

import androidx.annotation.NonNull;

import com.nd.android.adhoc.basic.common.exception.AdhocException;
import com.nd.android.adhoc.basic.log.Logger;
import com.nd.android.adhoc.basic.util.thread.AdhocRxJavaUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class MdmCmdManager {
    private static final String TAG = "MdmCmdManager";
    private static final List<String> sOrderlyCmd = new ArrayList<String>(){
        {
            add("mdm_camera_capture");
            add("start_record");
            add("stop_record");
            add("startmonitor");
            add("stopmonitor");
            //sendmsg 在mvp版本中暂时视为立刻执行
            //add("sendmsg");
            add("withdrawcommand");
        }
    };
    private static final ExecutorService sSingleThreadExecutor = Executors.newSingleThreadExecutor();

    public static void doCmdReceived(final String pData, final CommandFromTo pFrom, final CommandFromTo pTo) {
        MdmCmdContent cmdContent;
        try {
            // 指令解析
            cmdContent = MdmCmdParser.commandParsing(pData, pFrom, pTo, CommandType.CMD_TYPE_STATUS);
            Logger.i(TAG, "doCmdReceived: sessionId: " + cmdContent.getSessionId() + ", cmdName = " + cmdContent.getCmdName());


        } catch (Exception e) {
            Logger.e(TAG, "doCmdReceived, parsing cmd content error, result is not feedback: " + e);
            return;
        }

        //TODO: 指令入库
//        ICmdEntity cmdEntity = CmdEntityHelper.newCmdEntity(
//                cmdContent.getSessionId(),
//                cmdContent.getCmdJson().toString(),
//                cmdContent.getSendVersion(),
//                TimeFixApi.getInstance().getCurrentTime());
//
//        CmdDbOperatorFactory.getCmdDbOperator().saveOrUpdateEntity(cmdEntity);

        //指令执行,sOrderlyCmd包含不需要立刻执行的cmd
        if (cmdContent.isOrderly() || sOrderlyCmd.contains(cmdContent.getCmdName())) {
            executeCmdOrderly(cmdContent);
        } else {
            executeCmdConcurrent(cmdContent);
        }

    }

    private static void executeCmdConcurrent(final @NonNull MdmCmdContent cmdContent){
        Logger.i(TAG, "executeCmd, cmd: " + cmdContent.getCmdName() + ", sid: " + cmdContent.getSessionId());
        AdhocRxJavaUtil.safeSubscribe(
                Observable.create(new Observable.OnSubscribe<MdmCmdContent>() {
                    @Override
                    public void call(Subscriber<? super MdmCmdContent> subscriber) {
                        Logger.i(TAG, "executeCmdOrderly, start run cmdName: " + cmdContent.getCmdName() + ", sessionId: " + cmdContent.getSessionId());
                        execute(cmdContent);
                        subscriber.onCompleted();
                    }
                }).subscribeOn(Schedulers.io())
        );

    }

    private static void executeCmdOrderly(final MdmCmdContent cmdContent) {
        Logger.i(TAG, "executeCmdOrderly, cmd: " + cmdContent.getCmdName() + ", sid: " + cmdContent.getSessionId());
        sSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                // 这里去执行 通知 和 进行延迟操作
                Logger.i(TAG, "executeCmdOrderly, start run cmdName: " + cmdContent.getCmdName() + ", sessionId: " + cmdContent.getSessionId());

                execute(cmdContent);
            }
        });

    }

    private static void execute(final MdmCmdContent cmdContent) {
        //TODO: 检查设备是否已经enroll checkQuitExecution()
        List<Boolean> operateResults = new ArrayList<>();
        MdmCmdOperator operator = new MdmCmdOperator(new MdmCmdCreator(), new MdmCmdExecutor());
        try {
            operateResults.add(operator.operate(cmdContent));
        } catch (AdhocException e) {
            e.printStackTrace();
        }
    }
    
}