package com.netty.handler;


import com.netty.context.ServerContextHolder;
import com.netty.distribute.DistributedServerUtil;
import com.netty.exception.RespCodeEnum;
import com.netty.model.protobuf.ServerCommand;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author hu
 * @Date Create In 16:08 2019/1/9 0009
 * @Description: 处理登录请求, 此处可以加入登录校验逻辑
 */
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {
    protected static final Logger log = LoggerFactory.getLogger(LoginAuthRespHandler.class);

    Map<String, Channel> channelHolder = ServerContextHolder.channelHolder;

    Map<String, String> remoteClientHolder = ServerContextHolder.remoteClientHolder;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ServerCommand.CommandModel comm = (ServerCommand.CommandModel) msg;
        String remoteAddress = ctx.channel().remoteAddress().toString();
        ServerCommand.CommandModel loginResp = null;

        if ((msg instanceof ServerCommand.CommandModel) && comm.getMessageType() == ServerCommand.MessageType.LOGIN_REQ) {
            //参数校验
            if (comm.getClientId() == null || comm.getClientId().isEmpty()) {
                loginResp = buildResponse(comm.getClientId(), RespCodeEnum.REQUIRE_CLIENTID);
                ctx.writeAndFlush(loginResp);
                return;
            }

            //判断重复登录
            Channel channel = channelHolder.get(comm.getClientId());
            if (Objects.nonNull(channel)) {
                String oldRemoteAddress = channel.remoteAddress().toString();
                log.debug("remoteAddress={},oldRemoteAddress={}", remoteAddress, oldRemoteAddress);
                if (oldRemoteAddress.equals(remoteAddress)) {
                    loginResp = buildResponse(comm.getClientId(), RespCodeEnum.REPEAT_LOGIN);
                    ctx.writeAndFlush(loginResp);
                    return;
                }
            }

            //保存channel用于后续通讯
            channelHolder.put(comm.getClientId(), ctx.channel());
            //保存远程客户端连接信息到redis中
            DistributedServerUtil.addClient(comm.getClientId());
            //保存远程客户端信息
            remoteClientHolder.put(remoteAddress, comm.getClientId());
            loginResp = buildResponse(comm.getClientId(), RespCodeEnum.SUCCESS);
        } else {
            //判断是否登录
            if (remoteClientHolder.containsKey(remoteAddress)) {
                ctx.fireChannelRead(msg);
                return;
            } else {
                loginResp = buildResponse(comm.getClientId(), RespCodeEnum.NOT_LOGIN);
            }
        }

        ctx.writeAndFlush(loginResp);
    }


    private ServerCommand.CommandModel buildResponse(String clientId, RespCodeEnum respCodeEnum) {
        ServerCommand.CommandModel.Builder builder = ServerCommand.CommandModel.newBuilder();
        builder.setMessageType(ServerCommand.MessageType.LOGIN_RESP);
        builder.setResult(respCodeEnum.getRespCode());
        builder.setMessage(respCodeEnum.getRespInfo());
        builder.setClientId(DistributedServerUtil.getServerAddress());
        log.debug("send The login response is :result={},client={},builder={}",
                builder.getResult(), clientId, builder);
        return builder.build();
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        removeConn(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("异常错误：{}", cause.getMessage(), cause);
        removeConn(ctx);
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }


    private void removeConn(ChannelHandlerContext ctx) {
        String remoteAddress = ctx.channel().remoteAddress().toString();
        // 删除本地缓存
        String clientId = remoteClientHolder.remove(remoteAddress);
        if (Objects.nonNull(clientId)) {
            //服务器端保存的当前的连接
            String oldRemoteAddress = channelHolder.get(clientId).remoteAddress().toString();
            if (Objects.nonNull(oldRemoteAddress)) {

                log.debug("当前remoteAddress={},oldRemoteAddress={}", remoteAddress, oldRemoteAddress);
                //判断服务器保存的连接和需要删除的连接一致，从channelHolder中移除连接
                if (remoteAddress.equals(oldRemoteAddress)) {
                    log.debug("删除channel：clientId={}", clientId);
                    //删除对应的chanel
                    channelHolder.remove(clientId);
                    if (DistributedServerUtil.isConnectSelf(clientId)) {
                        log.debug("客户端连接在此服务器，从分布式缓存中删除，clientId={}", clientId);
                        DistributedServerUtil.removeClient(clientId);
                    }
                }
            }
            log.debug("remove channel = {},remoteAddress={}" + clientId, remoteAddress);
        }
    }
}
