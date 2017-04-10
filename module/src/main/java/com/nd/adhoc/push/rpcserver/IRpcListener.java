package com.nd.adhoc.push.rpcserver;
import com.nd.adhoc.push.nettyserver.channel.Channel;

public interface IRpcListener {
    void onClientDisconnected(Channel channel, String reason);

    void onClientConnect(Channel channel, boolean bssh);

    void onClientMessage(Channel channel, Message rpcPacket);
}