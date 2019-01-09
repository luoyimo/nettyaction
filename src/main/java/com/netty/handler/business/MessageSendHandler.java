package com.netty.handler.business;

import com.google.protobuf.Any;
import com.netty.context.ServerContextHolder;
import com.netty.model.protobuf.ServerCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author hu
 * @Description: 消息处理 仅用于代码示例  无实际用途
 * @Date Create In 16:08 2019/1/9 0009
 */
public class MessageSendHandler extends BaseBusinessHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, ServerCommand.CommandModel commandModel) {
        ServerCommand.CommandModel.Builder builder = ServerCommand.CommandModel.newBuilder();
        builder.setMessageType(ServerCommand.MessageType.SENDMESSAGE);
        builder.setMessage("hello");
        builder.setData(Any.getDefaultInstance());
        ServerContextHolder.channelHolder.get("127.0.0.1:8099").writeAndFlush(builder);


    }
}
