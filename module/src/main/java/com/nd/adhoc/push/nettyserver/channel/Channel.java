/**
 * Copyright 2015-2016 ND Inc. All rights reserved.
 * im-access 
 */
package com.nd.adhoc.push.nettyserver.channel;

import io.netty.channel.ChannelFuture;

public interface Channel {
    enum EncryptAlgo{
        XXTEA,
        AES256,
        AES128
    }

    ChannelFuture writeAndFlush(Object msg);
    ChannelFuture close();
    void flush();
    ChannelFuture write(Object msg);
    io.netty.channel.Channel nativeChannel();
    String remoteAddress();
    String localAddress();
    void setSymmetricCipher(EncryptAlgo algo, String key) throws Exception;
}
