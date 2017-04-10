/**
 * Copyright 2015-2016 ND Inc. All rights reserved.
 * im-access 
 */
package com.nd.adhoc.push.rpcserver.channelhandler;


import com.nd.adhoc.push.nettyserver.channel.NettyChannel;
import com.nd.adhoc.push.rpcserver.Message;

import java.nio.ByteOrder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 *
 */
public class RpcPacketEncoder extends MessageToByteEncoder<Message> {

    private final NettyChannel nettyChannel;

    public RpcPacketEncoder(NettyChannel nettyChannel) {

        this.nettyChannel = nettyChannel;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message rpcPacket, ByteBuf out) throws Exception {
        if (rpcPacket.getBodyBuilder() == null) {
            rpcPacket.getHeader().bodySize.set(0);
            out.writeBytes(rpcPacket.getHeader().getByteBuffer().order(ByteOrder.LITTLE_ENDIAN));
        } else {
            byte[] byteArray ;
            if (nettyChannel.getSymCypher()!=null){
                byteArray = nettyChannel.getSymCypher().encrypt(rpcPacket.getBodyBuilder().build().toByteArray());
            } else {
                byteArray = rpcPacket.getBodyBuilder().build().toByteArray();
            }
            rpcPacket.getHeader().bodySize.set(byteArray.length);
            ByteBuf bytebuf = Unpooled.wrappedBuffer(byteArray);
            out.writeBytes(rpcPacket.getHeader().getByteBuffer().order(ByteOrder.LITTLE_ENDIAN));
            out.writeBytes(bytebuf);
        }
    }

}
