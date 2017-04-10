package com.nd.adhoc.push.rpcserver;


import com.nd.adhoc.push.nettyserver.channel.NettyChannel;
import com.nd.adhoc.push.rpcserver.channelhandler.RpcClientChannelInitializer;
import com.nd.adhoc.push.tcpclient.TcpClient;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;


public class RpcClient {
    private static Logger log = LoggerFactory.getLogger("RpcClient");
    private static final TcpClient tcpClient;
    private static final TcpClient tcpClientSSL;
    private static final RpcClientChannelInitializer.IClientListener listener;

    static {
        listener = new RpcClientChannelInitializer.IClientListener() {
            @Override
            public NettyChannel onChannelInit(io.netty.channel.Channel channel, boolean bssh) {
                RpcChannel clientchannel = (RpcChannel) channel;
                clientchannel.nettyChannel = new NettyChannel(channel);
                return clientchannel.nettyChannel;
            }

            @Override
            public void onClientDisconnected(com.nd.adhoc.push.nettyserver.channel.Channel channel, String reason) {
                try {
                    RpcChannel clientChannel = (RpcChannel) channel.nativeChannel();
                    clientChannel.onClientDisconnected(channel, reason);
                } catch (Exception e) {
                    log.error("onRpcClientMessage exception {}", ExceptionUtils.getFullStackTrace(e));
                }
            }

            @Override
            public void onClientMessage(com.nd.adhoc.push.nettyserver.channel.Channel channel, Message msg) {
                try {
                    RpcChannel clientChannel = (RpcChannel) channel.nativeChannel();
                    clientChannel.onClientMessage(msg);
                } catch (Exception e) {
                    log.error("onRpcClientMessage exception {}", ExceptionUtils.getFullStackTrace(e));
                }
            }
        };
        tcpClientSSL = new TcpClient(RpcChannel.class, new RpcClientChannelInitializer(listener, TcpClient.createSSlContext()));
        tcpClient = new TcpClient(RpcChannel.class, new RpcClientChannelInitializer(listener, null));
    }

    public static RpcChannel createRpcChannel(String host, int port, final boolean bssl, final IRpcListener listener) {
        TcpClient client;
        if (bssl) {
            client = tcpClientSSL;
        } else {
            client = tcpClient;
        }
        ChannelFuture future = client.connectTo(host, port);
        final RpcChannel channel = (RpcChannel) future.channel();
        channel.connectFuture = future;
        channel.listener = listener;
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                try {
                    listener.onClientConnect(channel.nettyChannel, bssl);
                } catch (Exception e) {
                    log.error("onRpcChannelConnect exception {}", ExceptionUtils.getFullStackTrace(e));
                }
            }
        });
        return channel;
    }
}
