/**
 *
 */
package com.netty.handler;

import com.netty.model.protobuf.ServerCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @Author hu
 * @Description: 处理心跳消息
 * @Date Create In 16:17 2019/1/9 0009
 */
public class HeartBeatRespHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(HeartBeatRespHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ServerCommand.CommandModel comm = (ServerCommand.CommandModel) msg;
        if (Objects.equals(comm.getMessageType(), ServerCommand.MessageType.HEARTBEAT)) {
            ServerCommand.CommandModel.Builder heatBeat = ServerCommand.CommandModel.newBuilder();
            heatBeat.setClientId(comm.getClientId());
            heatBeat.setMessageType(ServerCommand.MessageType.HEARTBEAT);
            ctx.writeAndFlush(heatBeat.build());
            log.debug("HeartBeatRespHandler.channelRead:{}", heatBeat);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

}
