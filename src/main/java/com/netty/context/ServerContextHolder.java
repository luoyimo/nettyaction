package com.netty.context;

import com.netty.distribute.DistributedServerClient;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author hu
 * @Description: 缓存注册到服务端的客户端信息
 * @Date Create In 15:55 2019/1/8 0008
 */
public interface ServerContextHolder {
    /**
     * 保存channel,key为clientId,clientId为客户端唯一标识,可在客户端首次初始化时固定取mac地址或者uuid
     */
    Map<String, Channel> channelHolder = new ConcurrentHashMap<>(1024);

    /**
     * 保存远程客户端地址,key为远程客户端地址,value为clientId,可在客户端首次初始化时固定取mac地址或者uuid
     */
    Map<String, String> remoteClientHolder = new ConcurrentHashMap<>(1024);



    /**
     * 保存远程服务器地址,key为远程服务器地址，value为连接到服务端的服务端客户端实例
     * <p>
     * server1->client1->server2  通过服务端的客户端连接到远程的服务端
     */
    Map<String, DistributedServerClient> serverClientServer = new HashMap<>();


}
