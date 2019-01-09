package com.netty.distribute;


import com.netty.util.PropertiesUtil;
import org.redisson.api.RSet;

/**
 * @Author hu
 * @Description: 服务器之间消息转发
 * @Date Create In 15:55 2019/1/8 0008
 */
public interface RouterService {

    String REMOTESERVERSETKEY = "set:remote-server";

    String CLIENTPREFIX = "s:clientId:%s";


    String serverHostPort = PropertiesUtil.loadProperties().getProperty("server.local.address");


    /**
     * 新增连入到本服务器的客户端
     */
    void addClient(String clientId);


    /**
     * 移除远程客户端连接
     *
     * @param clientId
     */
    void removeClient(String clientId);


    /**
     * 查询出终端连入的服务器
     *
     * @param clientId
     */
    String findServer(String clientId);


    /**
     * 获取所有远程服务器
     *
     * @return
     */
    RSet<String> getServers();

    /**
     * 将本服务器连接到其他服务器 获取channel
     */
    void register();


}
