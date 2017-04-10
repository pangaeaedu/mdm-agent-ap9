package com.nd.adhoc.push.rpcserver.channelhandler;


import com.nd.adhoc.push.nettyserver.channel.NettyChannel;
import com.nd.adhoc.push.nettyserver.channelhandler.LengthFieldBasedFrameDecoder;
import com.nd.adhoc.push.rpcserver.Message;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteOrder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 *
 */
public class RpcClientChannelInitializer extends ChannelInitializer<Channel> {
    public interface IClientListener {
        NettyChannel onChannelInit(Channel channel, boolean bssh);

        void onClientDisconnected(com.nd.adhoc.push.nettyserver.channel.Channel channel, String reason);

        void onClientMessage(com.nd.adhoc.push.nettyserver.channel.Channel channel, Message msg);
    }

    private static Logger log = LoggerFactory.getLogger("RpcClientChannelInitializer");
    private IClientListener tcpListener;
    private SSLContext sslContext;

    public RpcClientChannelInitializer(IClientListener tcpListener, final SSLContext sslContext) {
        this.tcpListener = tcpListener;
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(final Channel channel) throws Exception {
        NettyChannel _nettyChannel = tcpListener.onChannelInit(channel, sslContext != null);
        ChannelPipeline p = channel.pipeline();

        if (sslContext != null) {
            SSLEngine engine = sslContext.createSSLEngine();
            engine.setUseClientMode(true);
            engine.setNeedClientAuth(true);
            SslHandler sslHandler = new SslHandler(engine);
            sslHandler.handshakeFuture().addListener(new GenericFutureListener<Future<? super Channel>>() {
                @Override
                public void operationComplete(Future<? super Channel> future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("client {} ssh handshake succeed", channel.remoteAddress().toString());
                    } else {
                        log.warn("client {} ssh handshake failed", channel.remoteAddress().toString());
                    }
                }
            });
            p.addLast("ssl", sslHandler);
        }
        p.addLast(ChannelHandlerType.FRAME_DECODER, new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 8192 * 3, 4, 4, 0, 0, true));
        p.addLast(ChannelHandlerType.PROTOBUF_DECODER, new RpcPacketDecoder(_nettyChannel));

        class HeartbeatHandler extends ChannelDuplexHandler {
            private final NettyChannel channel;
            int timeout_times = 0;

            HeartbeatHandler(NettyChannel channel) {
                this.channel = channel;
            }

            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                if (evt instanceof IdleStateEvent) {
                    IdleStateEvent e = (IdleStateEvent) evt;
                    Message packet = new Message(new
                            Message.PacketHDR((short) com.nd.sdp.im.protobuf.rpc.Package.OP.OP_HEARTBEAT_VALUE), null);
                    if (e.state() == IdleState.READER_IDLE) {
                        if (ctx.isRemoved()) {
                            return;
                        }
                        if (timeout_times++>2) {
                            ctx.close();
                            log.info("client {} timeout {} times , close it", channel.remoteAddress().toString(), timeout_times);
                        } else {
                            // 发送心跳探测
                            ctx.writeAndFlush(packet);
                        }

                    }
                    return;
                } else if (evt instanceof ChannelInputShutdownEvent) {
                    return;
                } else {
                    return;
                }
            }

            public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                tcpListener.onClientDisconnected(this.channel, "closed");
                ctx.close().awaitUninterruptibly();
            }

            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                boolean alreadyPrinted = false;
                if (cause instanceof IOException){
                    if (cause.getMessage()!=null && (cause.getMessage().equals("Connection reset by peer") ) ){
                        alreadyPrinted = true;
                        log.info("{} got exception {}",
                                ctx.channel().remoteAddress().toString(), ExceptionUtils.getFullStackTrace(cause));
                    }
                }
                if (!alreadyPrinted){
                    log.warn("{} got exception {}",
                            ctx.channel().remoteAddress().toString(),
                            ExceptionUtils.getFullStackTrace(cause));
                }

                if (cause instanceof DecoderException){
                    ctx.close().awaitUninterruptibly();
                }
            }
        }

        final HeartbeatHandler heartbeatHandler = new HeartbeatHandler(_nettyChannel);

        class ServerRpcPacketHandler extends RpcPacketHandler {

            NettyChannel channel;

            ServerRpcPacketHandler(NettyChannel channel){
                this.channel = channel;
            }

            @Override
            protected void handleRpcPacket(Message rpcPacket) {
                long bodySize = rpcPacket.getHeader().bodySize.get();
                tcpListener.onClientMessage(this.channel, rpcPacket);
                heartbeatHandler.timeout_times = 0;
            }
        }
        p.addLast(ChannelHandlerType.PROTOBUF_HANDLER, new ServerRpcPacketHandler(_nettyChannel));



        p.addLast(ChannelHandlerType.FRAME_ENCODER, new RpcPacketEncoder(_nettyChannel))
                .addLast("ping", new IdleStateHandler(60, 0, 0))
                .addLast("HeartbeatHandler", heartbeatHandler);
    }
}

