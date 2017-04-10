package com.nd.adhoc.push.rpcserver;

import com.google.protobuf.MessageLite;
import com.nd.adhoc.push.encrypt.SymmetricCipher;
import com.nd.sdp.im.protobuf.rpc.Package;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javolution.io.Struct;

/**
 * 消息结构
 */
public class Message {
    /**
     * 网络包头
     */
    public static class PacketHDR extends Struct {

        public PacketHDR() {
            this.flag.set(0x4E44);
            this.ver.set((short) 0x01);
            this.op.set((short) 0);
            this.bodySize.set(0);
        }

        public PacketHDR(short op) {
            this.flag.set(0x4E44);
            this.ver.set((short) 0x01);
            this.op.set(op);
            this.bodySize.set(0);
        }

        public PacketHDR(short op, short ver) {
            this.flag.set(0x4E44);
            this.ver.set(ver);
            this.op.set(op);
            this.bodySize.set(0);
        }

        @Override
        public ByteOrder byteOrder() {
            return ByteOrder.LITTLE_ENDIAN;
        }

        public final Unsigned16 flag = new Unsigned16();
        public final Unsigned8 ver = new Unsigned8();
        public final Unsigned8 op = new Unsigned8();
        public final Unsigned32 bodySize = new Unsigned32();
    }

    private PacketHDR header;
    private Package.Body.Builder bodyBuilder;

    public Message(int seq, int methodId, Package.Uri target, MessageLite msg) {
        bodyBuilder = Package.Body.newBuilder();
        this.header = new PacketHDR((short) Package.OP.OP_REQUEST_VALUE);
        Package.RequestMsg reqMsg =
                Package.RequestMsg.newBuilder().setMethodId(methodId).setData(msg.toByteString())
                        .setSeq(seq).build();
        if (target != null) {
            bodyBuilder.addTargets(target);
        }
        bodyBuilder.addMsgs(reqMsg.toByteString()).build();
    }

    public Message(Package.Uri target, Package.ResponseMsg msg) {
        bodyBuilder = Package.Body.newBuilder();
        this.header = new PacketHDR((short) Package.OP.OP_RESPONSE_VALUE);
        if (null != target) {
            bodyBuilder.addTargets(target);
        }
        bodyBuilder.addMsgs(msg.toByteString()).build();
    }

    public Message(PacketHDR header, Package.Body body) {
        this.header = header;
        if (body != null) {
            this.bodyBuilder = body.toBuilder();
        }
    }

    public PacketHDR getHeader() {
        return header;
    }

    public Package.Body.Builder getBodyBuilder() {
        return bodyBuilder;
    }

    public ByteBuffer toByteBuffer(SymmetricCipher cipher) throws Exception {
        byte[] byteBody;
        if (getBodyBuilder()!=null){
            if (cipher==null){
                byteBody = getBodyBuilder().build().toByteArray();
            } else {
                byteBody = cipher.encrypt(getBodyBuilder().build().toByteArray());
            }

        } else {
            byteBody = new byte[0];
        }

        getHeader().bodySize.set(byteBody.length);
        ByteBuffer byteBuffer = ByteBuffer.allocate(getHeader().size() + byteBody.length);
        byteBuffer.put(getHeader().getByteBuffer().order(ByteOrder.LITTLE_ENDIAN));
        byteBuffer.put(byteBody);
        return byteBuffer;
    }

    public static Message fromByteBuffer(ByteBuffer byteBuffer, SymmetricCipher cipher) {
        PacketHDR header = new PacketHDR();
        if (byteBuffer.remaining() < header.size()) {
            return null;
        }
        header.setByteBuffer(byteBuffer.order(ByteOrder.LITTLE_ENDIAN), byteBuffer.position());
        if (byteBuffer.remaining() < header.bodySize.get()) {
            return null;
        }
        try {
            Package.Body body;
            if (cipher!=null){
                byte []decoded ;
                decoded = cipher.decrypt(byteBuffer.array(), byteBuffer.position() + header.size(), (int) header.bodySize.get());
                body = Package.Body.newBuilder().mergeFrom(decoded).build();
            } else {
                body = Package.Body.newBuilder().mergeFrom(byteBuffer.array(), byteBuffer.position()+header.size(), (int) header.bodySize.get()).build();
            }
            return new Message(header, body);
        } catch (Exception e) {
            return null;
        }
    }
}