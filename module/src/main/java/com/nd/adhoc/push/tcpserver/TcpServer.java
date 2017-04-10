/**
 * Copyright 2015-2016 ND Inc. All rights reserved.
 * im-access 
 */
package com.nd.adhoc.push.tcpserver;


import com.nd.adhoc.push.util.RenameThreadFac;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


public class TcpServer {
//    private final static Logger log = LoggerFactory.getLogger("TcpServer");
    private ServerBootstrap bootstrap = new ServerBootstrap();
    private NioEventLoopGroup boss;
    private NioEventLoopGroup workers;

    private void init(String name, ChannelHandler channelHandler) {
        boss = new NioEventLoopGroup(8, new RenameThreadFac(name + "_boss"));
        workers = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 3,
                new RenameThreadFac(name + "_worker"));
        bootstrap.group(boss, workers);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.SO_SNDBUF, 1048576);
        bootstrap.option(ChannelOption.SO_RCVBUF, 1048576);
        bootstrap.childOption(ChannelOption.SO_RCVBUF, 1048576);
        bootstrap.childOption(ChannelOption.SO_SNDBUF, 1048576);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childHandler(channelHandler);
    }

    public TcpServer(String name, int port, ChannelHandler channelHandler) throws Exception {
        init(name, channelHandler);
        bootstrap.localAddress(port);
        io.netty.channel.ChannelFuture bindFuture = bootstrap.bind();
        bindFuture.awaitUninterruptibly();
        if (!bindFuture.isSuccess()){
            throw new Exception("bind to 0.0.0.0:"+port+" failed");
        } else {
//            log.info("binded to 0.0.0.0:{}", port);
        }
    }

    public TcpServer(String name, String host, int port, ChannelHandler channelHandler) throws Exception {
        init(name, channelHandler);
        bootstrap.localAddress(host, port);
        io.netty.channel.ChannelFuture bindFuture = bootstrap.bind();
        bindFuture.awaitUninterruptibly();
        if (!bindFuture.isSuccess()){
            throw new Exception("bind to "+host+":"+port+" failed");
        } else {
//            log.info("binded to {}:{}", host, port);
        }
    }

    public void stop() {
        bootstrap.childGroup().shutdownGracefully();
        boss.shutdownGracefully();
        workers.shutdownGracefully();
    }

    public static SSLContext createSSlContext() throws Exception {
        String jksPath = "ssl/sSdpIm.jks";
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream jksFileStream = TcpServer.class.getClassLoader().getResourceAsStream(jksPath);
        keyStore.load(jksFileStream, "ndsdpimsslserverkey".toCharArray());

        TrustManagerFactory tf = TrustManagerFactory.getInstance("SunX509");
        tf.init(keyStore);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, "ndsdpimsslserverkey".toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kmf.getKeyManagers(), tf.getTrustManagers(), null);
        return sslContext;
    }

}
