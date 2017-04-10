package com.nd.adhoc.push.tcpclient;


import com.nd.adhoc.push.util.RenameThreadFac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;

public class TcpClient {
    private static Logger log = LoggerFactory.getLogger("TcpClient");
    private Bootstrap bootstrap = new Bootstrap();
    private NioEventLoopGroup workers;

    public TcpClient(Class<? extends Channel> channel, ChannelHandler handler) {
        init(channel, handler);
    }

    private void init(Class<? extends Channel> channel, ChannelHandler handler) {
        workers = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 3,
                new RenameThreadFac("tcp_client_worker"));
        bootstrap.group(workers);
        bootstrap.channel(channel);
        bootstrap.option(ChannelOption.SO_SNDBUF, 1048576);
        bootstrap.option(ChannelOption.SO_RCVBUF, 1048576);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        bootstrap.handler(handler);
    }

    public ChannelFuture connectTo(String host, int port) {
        InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
        return bootstrap.connect(remoteAddress);
    }

    public void stop() {
        bootstrap.group().shutdownGracefully();
        workers.shutdownGracefully();
    }

    public static SSLContext createSSlContext() {
        try {
            String jksPath = "ssl/sSdpIm.jks";
            KeyStore keyStore = KeyStore.getInstance("JKS");
            InputStream jksFileStream = TcpClient.class.getClassLoader().getResourceAsStream(jksPath);
            keyStore.load(jksFileStream, "ndsdpimsslserverkey".toCharArray());

            TrustManagerFactory tf = TrustManagerFactory.getInstance("SunX509");
            tf.init(keyStore);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, "ndsdpimsslserverkey".toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), tf.getTrustManagers(), null);
            return sslContext;
        } catch (Exception e) {

            log.warn("create ssl context exception {}", e.toString());
            return null;
        }

    }
}
