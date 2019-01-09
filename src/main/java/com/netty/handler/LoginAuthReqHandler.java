package com.netty.handler;

import com.netty.model.protobuf.ServerCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


/**
 * @author hu
 * @Date Create In 16:08 2019/1/9 0009
 * @Description: 在注册channel时发起登录请求
 */
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {


    private String serverHostPort;

    public LoginAuthReqHandler(String serverHostPort) {
        this.serverHostPort = serverHostPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ServerCommand.CommandModel.Builder builder = ServerCommand.CommandModel.newBuilder();
        builder.setMessageType(ServerCommand.MessageType.LOGIN_REQ);
        builder.setClientId(serverHostPort);
        builder.setServerTime(System.currentTimeMillis());
        ctx.writeAndFlush(builder.build());
        ctx.fireChannelActive();
    }


}
