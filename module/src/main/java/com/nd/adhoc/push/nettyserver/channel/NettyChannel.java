/**
 * Copyright 2015-2016 ND Inc. All rights reserved.
 * im-access 
 */
package com.nd.adhoc.push.nettyserver.channel;


import com.nd.adhoc.push.encrypt.AES128;
import com.nd.adhoc.push.encrypt.AES256;
import com.nd.adhoc.push.encrypt.SymmetricCipher;
import com.nd.adhoc.push.encrypt.XXTEA;

import io.netty.channel.ChannelFuture;

public class NettyChannel implements Channel {
    private final io.netty.channel.Channel channel;

    private final String remoteAddr;

    private final String localAddr;

    private SymmetricCipher symCipher = null;

    public NettyChannel(io.netty.channel.Channel channel) {
        this.channel = channel;
        if (null!=channel.localAddress()) {
            this.localAddr = channel.localAddress().toString();
        } else {
            this.localAddr = null;
        }
        if (null!=channel.remoteAddress()) {
            this.remoteAddr = channel.remoteAddress().toString();
        } else {
            this.remoteAddr = null;
        }
    }

    public ChannelFuture writeAndFlush(Object msg) {
        return channel.writeAndFlush(msg);
    }

    public ChannelFuture close(){
        return channel.close();
    }

    public void flush(){
        channel.flush();
    }

    public io.netty.channel.Channel nativeChannel() {
        return channel;
    }

    public ChannelFuture write(Object msg){
        return channel.write(msg);
    }

    public String remoteAddress(){
        if (null==this.remoteAddr) {
            if (null!=channel.remoteAddress()) {
                return channel.remoteAddress().toString();
            }
            return "";
        }
        return channel.remoteAddress().toString();
    }

    public String localAddress(){
        if (null==this.localAddr) {
            if (null!=channel.localAddress()) {
                return channel.localAddress().toString();
            }
            return "";
        }
        return channel.localAddress().toString();
    }

    @Override
    public void setSymmetricCipher(EncryptAlgo algo, String key) throws Exception {
        if (algo.equals(EncryptAlgo.XXTEA)){
            symCipher = new XXTEA();
        } else if (algo.equals(EncryptAlgo.AES256)){
            symCipher = new AES256();
        } else if (algo.equals(EncryptAlgo.AES128)){
            symCipher = new AES128();
        }
        symCipher.init(key);

    }

    public SymmetricCipher getSymCypher() {
        return symCipher;
    }
}
