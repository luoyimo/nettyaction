package com.netty.handler.business;

import com.netty.model.protobuf.ServerCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Author hu
 * @Description: 无实际用途
 * @Date Create In 16:17 2019/1/9 0009
 */
public class SendMessageHandler extends ChannelInboundHandlerAdapter {

    private String serverHostPort;

    public SendMessageHandler(String serverHostPort) {
        this.serverHostPort = serverHostPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ServerCommand.CommandModel.Builder builder = ServerCommand.CommandModel.newBuilder();
        builder.setMessageType(ServerCommand.MessageType.SENDMESSAGE);
        builder.setClientId(serverHostPort);
        builder.setServerTime(System.currentTimeMillis());
        ctx.writeAndFlush(builder.build());
        ctx.fireChannelActive();
    }
}
