package com.netty;

import com.netty.context.HandlerName;
import com.netty.distribute.RouterService;
import com.netty.handler.HeartBeatRespHandler;
import com.netty.handler.LoginAuthRespHandler;
import com.netty.handler.business.MessageSendHandler;
import com.netty.model.protobuf.ServerCommand;
import com.netty.util.PropertiesUtil;
import com.netty.util.RedisUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    public static void main(String[] args) {
        String severAddress = PropertiesUtil.getProperty("server.local.address");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            RedisUtil.removeFromSet(RouterService.REMOTESERVERSETKEY, RouterService.serverHostPort);
        }));
        new NettyServer().bind(Integer.parseInt(severAddress.split(":")[1]));

    }


    public void bind(int port) {
        //配置服务器NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            ch.pipeline().addLast(new ProtobufDecoder(ServerCommand.CommandModel.getDefaultInstance()));
                            ch.pipeline().addLast(new ProtobufEncoder());
                            //10秒后没有读取到任何消息就自动断开连接
                            ch.pipeline().addLast(HandlerName.READTIMEOUT, new ReadTimeoutHandler(10));
                            //登录请求处理
                            ch.pipeline().addLast(HandlerName.LOGINAUTHREQ, new LoginAuthRespHandler());
                            //心跳检测处理
                            ch.pipeline().addLast(HandlerName.HEARTBEAT, new HeartBeatRespHandler());
                            //后面这段代码可以删除 此处用于测试
                            ch.pipeline().addLast(ServerCommand.MessageType.SENDMESSAGE.name(), new MessageSendHandler());

                        }
                    });
            //绑定端口， 同步等待成功
            ChannelFuture f = b.bind(port).sync();
            //等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("异常中断：{}", e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
