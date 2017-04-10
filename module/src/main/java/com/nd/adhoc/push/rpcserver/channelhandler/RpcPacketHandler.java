/**
 * Copyright 2015-2016 ND Inc. All rights reserved.
 * im-access 
 */
package com.nd.adhoc.push.rpcserver.channelhandler;


import com.nd.adhoc.push.rpcserver.Message;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * 客户端向服务端发起请求时的 ChannelHandler
 */
public abstract class RpcPacketHandler extends MessageToMessageDecoder<Message> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Message rpcPacket, List<Object> list) throws Exception {
        this.handleRpcPacket(rpcPacket);
    }

    protected abstract void handleRpcPacket(Message rpcPacket);
}
