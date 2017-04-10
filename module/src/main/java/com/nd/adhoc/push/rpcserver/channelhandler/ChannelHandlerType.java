/**
 * Copyright 2015-2016 ND Inc. All rights reserved.
 * im-access 
 */
package com.nd.adhoc.push.rpcserver.channelhandler;

/**
 * 列出所有Netty ChannelHandler的类型名称
 */
public class ChannelHandlerType {
    public static final String FRAME_DECODER = "frameDecoder";
    public static final String FRAME_ENCODER = "frameEncoder";
    public static final String PROTOBUF_DECODER = "protobufDecoder";
    public static final String PROTOBUF_HANDLER = "protobufHandler";
}
