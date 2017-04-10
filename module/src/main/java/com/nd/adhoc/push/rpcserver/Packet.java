package com.nd.adhoc.push.rpcserver;

import java.nio.ByteBuffer;

/**
 * 消息结构
 */
public class Packet {
    public Packet(Message.PacketHDR header, ByteBuffer byteBuffer) {
        this.header = header;
        this.byteBuffer = byteBuffer;
    }

    public Message.PacketHDR getHeader() {
        return header;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    private final Message.PacketHDR header;
    private final ByteBuffer byteBuffer;

}