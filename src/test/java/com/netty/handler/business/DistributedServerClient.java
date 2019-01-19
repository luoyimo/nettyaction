package com.netty.handler.business;

import com.netty.context.HandlerName;
import com.netty.context.ServerContextHolder;
import com.netty.handler.HeartBeatServerReqHandler;
import com.netty.handler.LoginAuthReqHandler;
import com.netty.model.protobuf.ServerCommand;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.Thread.sleep;


/**
 * @Author hu
 * @Description: 服务器之间通信的客户端，用于服务器之间通信，多服务器之间转发
 * @Date Create In 15:55 2019/1/8 0008
 */
public class DistributedServerClient {


    private static final Logger logger = LoggerFactory.getLogger(DistributedServerClient.class);

    private ThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private String host;
    private int port;
    //服务器之间连接的channel
    private volatile Channel channel;
    private ChannelFuture future;
    private HeartBeatServerReqHandler heartBeatReqHandler;
    private EventLoopGroup group;
    private volatile boolean connected = false;
    private String hostPort;

    public DistributedServerClient(String address) {
        hostPort = address;
        String[] hostPort = address.split(":");
        this.host = hostPort[0];
        this.port = Integer.parseInt(hostPort[1]);
    }

    public Channel getChannel() {
        return channel;
    }


    public void connect() {

        //配置客户端NIO线程组
        group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            ch.pipeline().addLast(new ProtobufDecoder(ServerCommand.CommandModel.getDefaultInstance()));
                            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            ch.pipeline().addLast(new ProtobufEncoder());
                            //10s内没有获取到任何响应,超时中断,抛出异常
                            ch.pipeline().addLast(HandlerName.READTIMEOUT, new ReadTimeoutHandler(10));
                            //登录到服务器端
                            ch.pipeline().addLast(HandlerName.LOGINAUTHREQ, new LoginAuthReqHandler("localhost"));
                            //心跳检测
                            heartBeatReqHandler = new HeartBeatServerReqHandler();
                            ch.pipeline().addLast(HandlerName.HEARTBEAT, heartBeatReqHandler);

                            //后面这段代码可以删除 用于测试
                            ch.pipeline().addLast(ServerCommand.MessageType.SENDMESSAGE.name(), new SendMessageHandler("localhost"));

                            channel = ch;
                        }
                    });

            //发起异步连接
            future = b.connect(host, port).sync();
            connected = true;
            //等待客户端链路关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("服务端客户端异常中断：{}", e.getMessage(), e);
            connected = false;
        } finally {
            logger.info("发起重连操作,当前服务器：host:{},port:{}", host, port);
            //关闭定时任务
            close();
            //重连
            reconnect();
        }
    }

    //关闭链接
    public void close() {
        connected = false;
        heartBeatReqHandler.stopHeartBeat();
        group.shutdownGracefully();
        channel = null;
        ServerContextHolder.serverClientServer.remove(hostPort);
    }

    public void reconnect() {
        if (!connected) {
            executor.execute(() -> {
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    logger.error("重连失败：{}", e.getMessage(), e);
                }
                connect();

            });
        }
    }

}
