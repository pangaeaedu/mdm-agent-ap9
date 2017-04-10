package com.nd.adhoc.push.rpcserver;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.gs.collections.api.block.function.Function;
import com.nd.adhoc.push.nettyserver.channel.Channel;
import com.nd.adhoc.push.nettyserver.channel.NettyChannel;
import com.nd.sdp.im.protobuf.rpc.Package;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioSocketChannel;

public class RpcChannel extends NioSocketChannel {
    private static Logger log = LoggerFactory.getLogger("RpcClientChannel");
    IRpcListener listener = null;
    ChannelFuture connectFuture = null;
    NettyChannel nettyChannel;
    private AtomicInteger reqSeq = new AtomicInteger(0);
    private static TimeoutChecker timeoutChecker = new TimeoutChecker("RpcTimeout");

    private class RpcItem {
        final Function<Package.ResponseMsg, Void> callback;

        RpcItem(Function<Package.ResponseMsg, Void> callback) {
            this.callback = callback;
        }
    }

    private ConcurrentHashMap<Integer, RpcItem> pendintRequests = new ConcurrentHashMap<>();

    public synchronized void rpcRequest(int methodId,
                                        final Package.Uri from,
                                        Package.Uri to,
                                        MessageLite reqmsg,
                                        int timeoutMills,
                                        final Function<Package.ResponseMsg, Void> callback) {
        do {
            final int seq = reqSeq.addAndGet(1);
            if (null != pendintRequests.putIfAbsent(seq, new RpcItem(callback))) {
                log.warn("rpc duplicated seq {}", seq);
                continue;
            }
            Message packet = new Message(seq, methodId, to, reqmsg);
            packet.getBodyBuilder().setFrom(from);
            nettyChannel.writeAndFlush(packet);
            timeoutChecker.pushItem(new TimeoutChecker.TimeOutItem() {
                @Override
                public boolean isExpire() {
                    return true;
                }

                @Override
                public void expired() {
                    if (pendintRequests.remove(seq) != null) {
                        log.warn("rpc {} timeout", seq);
                        callback.valueOf(
                                Package.ResponseMsg.newBuilder().setErrCode(Package.Errors.EC_RPC_REQUEST_TIMEOUT_VALUE)
                                        .setErrMsg("TIMEOUT")
                                        .setSeq(seq).build()
                        );
                    }
                }
            }, timeoutMills);
            log.info("rpc {} sent, methodid {}", seq, methodId);
            break;
        } while (true);

    }

    void onClientMessage(Message msg) {
        if (Package.OP.OP_RESPONSE_VALUE == msg.getHeader().op.get()) {
            for (ByteString msgItem : msg.getBodyBuilder().getMsgsList()) {
                try {
                    Package.ResponseMsg responseMsg = Package.ResponseMsg.parseFrom(msgItem.toByteArray());
                    RpcItem item = pendintRequests.remove(responseMsg.getSeq());
                    log.info("rpc {} response", responseMsg.getSeq());
                    if (null != item) {
                        item.callback.valueOf(responseMsg);
                    }
                } catch (InvalidProtocolBufferException e) {
                    log.warn("parse response failed , {}", ExceptionUtils.getFullStackTrace(e));
                }
            }
        }
        listener.onClientMessage(nettyChannel, msg);
    }

    void onClientDisconnected(Channel channel, String reason) {
        listener.onClientDisconnected(channel, reason);
    }
}