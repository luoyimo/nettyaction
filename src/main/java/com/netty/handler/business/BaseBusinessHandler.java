package com.netty.handler.business;

import com.netty.NettyServer;
import com.netty.model.protobuf.ServerCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author hu
 * @Description: 业务处理基类, 继承基类, 实现handle可自定义消息处理
 * 可参考{@link MessageSendHandler#handle(ChannelHandlerContext, ServerCommand.CommandModel)}
 * {@link NettyServer#bind(int)}
 * @Date Create In 16:08 2019/1/9 0009
 */
public abstract class BaseBusinessHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(BaseBusinessHandler.class);


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ServerCommand.CommandModel command = (ServerCommand.CommandModel) msg;
        if (Objects.equals(ctx.name(), command.getMessageType().name())) {
            log.info("客户端id:{},消息类型:{},消息体:{}", command.getClientId(), command.getMessageType(), command.getData());
            this.handle(ctx, command);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 处理消息
     *
     * @param ctx
     * @param commandModel
     */
    public abstract void handle(ChannelHandlerContext ctx, ServerCommand.CommandModel commandModel);


}
