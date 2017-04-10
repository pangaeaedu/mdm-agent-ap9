/**
 * Copyright 2015-2016 ND Inc. All rights reserved.
 * im-access 
 */
package com.nd.adhoc.push.rpcserver.channelhandler;

import com.google.protobuf.MessageLite;
import com.nd.adhoc.push.nettyserver.channel.NettyChannel;
import com.nd.adhoc.push.rpcserver.Message;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import com.nd.sdp.im.protobuf.rpc.Package;

/**
 *
 */
public class RpcPacketDecoder extends MessageToMessageDecoder<ByteBuf> {
    private static final Logger log = LoggerFactory.getLogger("RpcPacketDecoder");
    private static final boolean HAS_PARSER;
    private final MessageLite prototype;
    private final NettyChannel nettyChannel;

    public RpcPacketDecoder(NettyChannel nettyChannel) {
        this.nettyChannel = nettyChannel;
        this.prototype = Package.Body.getDefaultInstance();
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // 解析自定的头部
        Message.PacketHDR header = new Message.PacketHDR();
        int length = msg.readableBytes();
        byte[] array;
        int offset;
        if (msg.hasArray()) {
            array = msg.array();
            offset = msg.arrayOffset() + msg.readerIndex();
        } else {
            array = new byte[length];
            msg.getBytes(msg.readerIndex(), array, 0, length);
            offset = 0;
        }
        if (length < header.size()) {
            throw new DecoderException("length is less than headersize");
        }
        header.setByteBuffer(ByteBuffer.wrap(array, offset, header.size()).order(ByteOrder.LITTLE_ENDIAN), 0);
        if (length < header.size() + header.bodySize.get()) {
            throw new DecoderException("length is less than headersize+bodysize");
        }

        offset += header.size();
        length = (int) header.bodySize.get();

        if (length>0){
            Message packet;
            if (nettyChannel.getSymCypher()!=null){
                byte [] decrypted = nettyChannel.getSymCypher().decrypt(array, offset, length);
                if (HAS_PARSER) {
                    packet =
                            new Message(header,
                                    (Package.Body) this.prototype.getParserForType().parseFrom(decrypted));
                } else {
                    packet =
                            new Message(header,
                                    (Package.Body) this.prototype.newBuilderForType().mergeFrom(decrypted).build());
                }
            } else {
                if (HAS_PARSER) {
                    packet =
                            new Message(header,
                                    (Package.Body) this.prototype.getParserForType().parseFrom(array, offset, length));
                } else {
                    packet =
                            new Message(header,
                                    (Package.Body) this.prototype.newBuilderForType().mergeFrom(array, offset, length).build());
                }
            }

            out.add(packet);
        } else {
            out.add(new Message(header, null));
        }

    }

    static {
        boolean hasParser;
        try {
            MessageLite.class.getDeclaredMethod("getParserForType");
            hasParser = true;
        } catch (Exception var2) {
            log.info("protobuf no parser method , {}", ExceptionUtils.getMessage(var2));
            hasParser = false;
        }
        HAS_PARSER = hasParser;
    }
}
