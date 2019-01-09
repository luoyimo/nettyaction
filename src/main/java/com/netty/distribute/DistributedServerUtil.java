package com.netty.distribute;

import com.netty.context.ServerContextHolder;
import com.netty.exception.RouterException;
import com.netty.model.protobuf.ServerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @Author hu
 * @Description: 服务器之间通信工具类，用于服务器之间通信判断
 * @Date Create In 15:55 2019/1/8 0008
 */
public class DistributedServerUtil implements ServerContextHolder {

    private static final Logger logger = LoggerFactory.getLogger(DistributedServerUtil.class);


    private static RouterService routerService = new RouterServiceRedisImpl();


    /**
     * 是否已经连接到远程服务器或者远程服务器本身
     *
     * @param hostPort
     * @return
     */
    public static boolean isExistServerClient(String hostPort) {
        return !Objects.isNull(serverClientServer.get(hostPort))
                || Objects.equals(hostPort, RouterService.serverHostPort);
    }

    /**
     * 该服务器是否在线
     *
     * @param hostPort
     * @return
     */
    public static boolean isServerOnline(String hostPort) {

        boolean onLocalServer = Objects.equals(hostPort, RouterService.serverHostPort);
        if (onLocalServer) {
            return true;
        }
        //其他服务器
        DistributedServerClient serverClient = serverClientServer.get(hostPort);

        boolean onOtherServer = Objects.nonNull(serverClient) && Objects.nonNull(serverClient.getChannel());

        return onOtherServer;
    }


    /**
     * 把客户端关联到本服务器中
     *
     * @param clientId
     */
    public static void addClient(String clientId) {
        routerService.addClient(clientId);
    }

    /**
     * 在服务器关联列表中移除设备
     *
     * @param clientId
     */
    public static void removeClient(String clientId) {
        routerService.removeClient(clientId);
    }


    /**
     * 获取当前服务器的地址
     *
     * @return
     */
    public static String getServerAddress() {
        return RouterService.serverHostPort;
    }


    /**
     * 判断远程客户端是否在本服务器上
     *
     * @param clientId
     * @return
     */
    public static boolean isConnectSelf(String clientId) {
        String hostPort = routerService.findServer(clientId);
        return Objects.equals(hostPort, RouterService.serverHostPort);
    }


    /**
     * 查询远程客户端链接到的服务器地址
     *
     * @param clientId
     * @return
     */
    public static String findServer(String clientId) {
        return routerService.findServer(clientId);
    }


    /**
     * 生成新的服务器链接
     *
     * @param hostPort 格式ip:prot
     */
    public synchronized static void connectRemoteServer(String hostPort) {
        if (!DistributedServerUtil.isExistServerClient(hostPort)) {
            DistributedServerClient client = new DistributedServerClient(hostPort);
            try {
                client.connect();
            } catch (Exception e) {
                logger.error("connect error:{}", e.getMessage(), e);
                return;
            }
            serverClientServer.put(hostPort, client);
        }
    }


    /**
     * 删除服务器链接
     *
     * @param hostPort
     */
    public static void delClient(String hostPort) {
        DistributedServerClient client = serverClientServer.remove(hostPort);
        client.close();

    }


    /**
     * 发送任务到指定服务器
     */
    public static boolean sendTask(ServerCommand.CommandModel.Builder builder, String clientId) throws RouterException {
        if (Objects.isNull(builder)) {
            return false;
        }

        ServerCommand.CommandModel commandModel = build(builder, clientId);

        if (Objects.isNull(commandModel.getDistributeData().getToServer())) {
            throw new RouterException("目标地址不能为空");
        }

        DistributedServerClient client = serverClientServer.get(commandModel.getDistributeData().getToServer());
        if (Objects.isNull(client) || Objects.isNull(client.getChannel())) {
            return false;
        }
        client.getChannel().writeAndFlush(commandModel);
        return true;
    }


    /**
     * 设置服务器内部路由信息
     *
     * @param builder
     * @param clientId
     * @return
     */
    private static ServerCommand.CommandModel build(ServerCommand.CommandModel.Builder builder, String clientId) {
        ServerCommand.CommandRouterModel.Builder commandRouter = ServerCommand.CommandRouterModel.newBuilder();
        commandRouter.setFromServer(DistributedServerUtil.getServerAddress());
        commandRouter.setToServer(DistributedServerUtil.findServer(clientId));
        commandRouter.setToClientId(clientId);
        builder.setDistributeData(commandRouter);
        return builder.build();
    }


}
