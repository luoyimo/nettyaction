package com.netty.handler;

import com.netty.distribute.DistributedServerUtil;
import com.netty.exception.RespCodeEnum;
import com.netty.model.protobuf.ServerCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author hu
 * @Date Create In 16:08 2019/1/9 0009
 * @Description: 登录成功返回消息处理
 */
public class HeartBeatServerReqHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatServerReqHandler.class);

    private volatile ScheduledFuture<?> heartBeat;


    private String clinetId;

    public HeartBeatServerReqHandler(String clientId) {
        this.clinetId = clientId;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ServerCommand.CommandModel comm = (ServerCommand.CommandModel) msg;
        if (Objects.equals(comm.getMessageType(), ServerCommand.MessageType.LOGIN_RESP)) {
            logger.debug("message content:{}", msg);
            if (Objects.equals(comm.getResult(), RespCodeEnum.SUCCESS.getRespCode())) {
                heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatServerReqHandler.HeartBeatTask(ctx), 0,
                        2000, TimeUnit.MILLISECONDS);
            }
        } else {
            ctx.fireChannelRead(msg);
        }

    }

    private class HeartBeatTask implements Runnable {
        private final ChannelHandlerContext ctx;

        public HeartBeatTask(final ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            ServerCommand.CommandModel.Builder heartBeat = ServerCommand.CommandModel.newBuilder();
            heartBeat.setMessageType(ServerCommand.MessageType.HEARTBEAT);
            heartBeat.setClientId(DistributedServerUtil.getServerAddress());
            ctx.writeAndFlush(heartBeat.build());
            logger.debug("send heartBeat:{}", heartBeat.build());

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        stopHeartBeat();
        ctx.fireExceptionCaught(cause);

    }

    public void stopHeartBeat() {
        if (heartBeat != null) {
            heartBeat.cancel(true);
            heartBeat = null;
        }
    }

}
