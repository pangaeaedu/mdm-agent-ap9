package com.nd.adhoc.push.pushsdk;


import com.google.protobuf.ByteString;
import com.gs.collections.api.block.function.Function;
import com.nd.adhoc.push.nettyserver.channel.Channel;
import com.nd.adhoc.push.rpcserver.IRpcListener;
import com.nd.adhoc.push.rpcserver.Message;
import com.nd.adhoc.push.rpcserver.RpcChannel;
import com.nd.adhoc.push.rpcserver.RpcClient;
import com.nd.sdp.im.common.StructTransfer;
import com.nd.sdp.im.protobuf.rpc.Common;
import com.nd.sdp.im.protobuf.rpc.Package;
import com.nd.sdp.im.protobuf.rpc.Push;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class PushSdk {
    private RpcClient client = new RpcClient();

    private static Logger log = LoggerFactory.getLogger("PushSdk");

    private boolean started = false;

    private String deviceid;

    private String appid;

    private RpcChannel rpcChannel = null;

    private long lastReconnectMs = 0;

    private static long reconnectIntervalMs = 5000;

    private List<PushSdkCallback> mPushCallbackList = new ArrayList<>();

    private AtomicBoolean mIsConnected = new AtomicBoolean(false);

    private String mIp;

    private int mPort;

    public PushSdk(String ip, int port){
        mIp = ip;
        mPort = port;
    }

    /**
     * push消息回调接口
     * 需要注册推送的组件需要将自己的callback
     * 注册进来
     * @param callback
     */
    public void addPushSdkCallback(PushSdkCallback callback) {
        mPushCallbackList.add(callback);
    }

    public void startPushSdk(String deviceid, String appid) {
        synchronized (this) {
            log.warn("start push sdk , deviceid {}, appid {} , started {}", deviceid, appid, started);
            if (started) {
                return;
            }
            started = true;
//            this.mPushSdkCallback = callback;
            this.deviceid = deviceid;
            this.appid = appid;
        }
        startConnect();
    }

    public String getDeviceid() {
        return deviceid;
    }

    //Todo
    public void restartPushSdk() {
        log.warn("restart push sdk , deviceid {}, appid {} , started {}", deviceid, appid, started);
        if (null != rpcChannel) {
            rpcChannel.close();
        }
    }

    //Todo
    public void stop() {
        restartPushSdk();
        started = false;
    }

    //Todo
    private void startConnect() {
        if (System.currentTimeMillis() - lastReconnectMs < reconnectIntervalMs) {
            try {
                Thread.sleep(reconnectIntervalMs - (System.currentTimeMillis() - lastReconnectMs));
            } catch (InterruptedException e) {
                log.warn("sleep exception {}", ExceptionUtils.getFullStackTrace(e));
            }
        }

        lastReconnectMs = System.currentTimeMillis();
        IRpcListener listener =
                new IRpcListener() {
                    @Override
                    public void onClientDisconnected(Channel channel, String reason) {
                        notifyClientConnectStatus(false);
                        if (started) {
                            startConnect();
                        }
                    }

                    @Override
                    public void onClientConnect(final Channel channel, boolean bssh) {
                        log.warn("push sdk connected, localaddr {} remoteaddr {}", channel.localAddress(), channel.remoteAddress());
                        try {
                            Push.PushRegisterRequest registerRequest = Push.PushRegisterRequest.newBuilder().setAppid(appid).build();
                            if (rpcChannel != null) {
                                rpcChannel.rpcRequest(
                                        Push.CmdIDs.CmdID_PushRegister_VALUE,
                                        StructTransfer.TransferDevice(Common.UriResourceDevice.newBuilder().setDeviceid(deviceid).build(), "access"),
                                        Package.Uri.getDefaultInstance(),
                                        registerRequest,
                                        5000,
                                        new Function<Package.ResponseMsg, Void>() {
                                            @Override
                                            public Void valueOf(Package.ResponseMsg responseMsg) {
                                                if (responseMsg.hasErrCode()) {
                                                    log.warn("push sdk register device id failed, deviceid {} , appid {} , errcode {}, errmsg {}",
                                                            deviceid, appid, responseMsg.getErrCode(), responseMsg.getErrMsg());
                                                    channel.close();
                                                } else {
                                                    log.info("push sdk register device id success, deviceid {} , appid {} ", deviceid, appid);
                                                    notifyClientConnectStatus(true);
                                                }
                                                return null;
                                            }
                                        });
                            }
                        } catch (Exception e) {
                            log.warn("push sdk send register req failed, {}", ExceptionUtils.getFullStackTrace(e));
                            channel.close();
                        }
                    }

                    @Override
                    public void onClientMessage(final Channel channel, Message rpcPacket) {
                        notifyClientConnectStatus(true);
                        if (rpcPacket.getHeader().op.get() == Package.OP.OP_REQUEST_VALUE) {
                            Package.Body body = rpcPacket.getBodyBuilder().build();
                            // 处理请求
                            for (ByteString msgItem : body.getMsgsList()) {
                                Package.RequestMsg reqMsg;
                                try {
                                    reqMsg = Package.RequestMsg.parseFrom(msgItem);
                                } catch (Exception e) {
                                    log.warn("parse request msg exception, exception:{}",
                                            ExceptionUtils.getFullStackTrace(e));
                                    continue;
                                }

                                if (reqMsg.getMethodId() == Push.CmdIDs.CmdID_PushMsg_VALUE) {
                                    try {
                                        final Push.PushMsgNotify notify = Push.PushMsgNotify.parseFrom(reqMsg.getData());

                                        log.warn("get push msg, deviceid {} , appid {}, msgid {}, contentlen {}",
                                                deviceid, appid, notify.getMsgid(), notify.getContent().size());

                                        Push.PushMsgAckRequest.Builder ackbuilder = Push.PushMsgAckRequest.newBuilder();
                                        notifyPushMessage(notify,ackbuilder);
//                                        final byte[] responseContent =
//                                                mPushSdkCallback.onPushMessage(notify.getAppid(), notify.getContent().toByteArray());
//                                        ackbuilder.setMsgid(notify.getMsgid());
//                                        ackbuilder.setAppid(notify.getAppid());
//                                        int len = 0;
//                                        if (null != responseContent) {
//                                            len = responseContent.length;
//                                            ackbuilder.setAckContent(ByteString.copyFrom(responseContent));
//                                        }

//                                        log.warn("push msg response, deviceid {} , appid {}, msgid {}, responselen {}",
//                                                deviceid, appid, notify.getMsgid(), len);

                                        try {
                                            rpcChannel.rpcRequest(
                                                    Push.CmdIDs.CmdID_PushAck_VALUE,
                                                    StructTransfer.TransferDevice(Common.UriResourceDevice.newBuilder().setDeviceid(deviceid).build(), "access"),
                                                    Package.Uri.getDefaultInstance(),
                                                    ackbuilder.build(),
                                                    5000,
                                                    new Function<Package.ResponseMsg, Void>() {
                                                        @Override
                                                        public Void valueOf(Package.ResponseMsg responseMsg) {
                                                            if (responseMsg.hasErrCode()) {
                                                                log.warn("push sdk ack msg failed, deviceid {} , appid {}, msgid {}, errorcode {} , errormsg {}",
                                                                        deviceid, appid, notify.getMsgid(), responseMsg.getErrCode(), responseMsg.getErrMsg());
                                                            } else {
                                                                log.info("push sdk ack msg success, deviceid {} , appid {}, msgid {}",
                                                                        deviceid, appid, notify.getMsgid());
                                                            }
                                                            return null;
                                                        }
                                                    });
                                        } catch (Exception e) {
                                            log.warn("push sdk ack msg failed, deviceid {} , appid {}, msgid {}, exception {}",
                                                    deviceid, appid, notify.getMsgid(), ExceptionUtils.getFullStackTrace(e));
                                        }

                                    } catch (Exception e) {
                                        log.warn("parse msg notify exception, exception:{}",
                                                ExceptionUtils.getFullStackTrace(e));
                                    }
                                }
                            }
                        } else if (rpcPacket.getHeader().op.get() == Package.OP.OP_HEARTBEAT_VALUE) {
                            Message packet = new Message(new
                                    Message.PacketHDR((short) com.nd.sdp.im.protobuf.rpc.Package.OP.OP_HEARTBEAT_ACK_VALUE), null);
                            channel.writeAndFlush(packet);
                        }
                    }
                };

        rpcChannel = client.createRpcChannel(mIp, mPort, false, listener);
    }

    private void notifyClientConnectStatus(boolean isConnected) {
        mIsConnected.set(isConnected);
        for (PushSdkCallback callback : mPushCallbackList) {
            callback.onClientConnected(isConnected);
        }
    }

    private void notifyPushMessage(Push.PushMsgNotify notify,Push.PushMsgAckRequest.Builder ackbuilder) {
        byte[] responseContent;
        for (PushSdkCallback callback : mPushCallbackList) {
            responseContent = callback.onPushMessage(notify.getAppid(), notify.getContent().toByteArray());
            replyAckContent(ackbuilder,notify,responseContent);
        }
    }

    /**
     * 回复确认消息
     * @param ackbuilder
     * @param notify
     * @param responseContent
     */
    private void replyAckContent(Push.PushMsgAckRequest.Builder ackbuilder,Push.PushMsgNotify notify, byte[] responseContent) {
        ackbuilder.setMsgid(notify.getMsgid());
        ackbuilder.setAppid(notify.getAppid());
        if (null != responseContent) {
            ackbuilder.setAckContent(ByteString.copyFrom(responseContent));
        }
    }

    public boolean isConnected() {
        return mIsConnected.get();
    }

}
