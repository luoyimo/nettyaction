package com.netty.distribute;

import com.netty.util.RedisUtil;
import org.redisson.api.RSet;

import java.util.Iterator;

/**
 * @Author hu
 * @Description: 服务端路由服务
 * @Date Create In 15:55 2019/1/8 0008
 */
public class RouterServiceRedisImpl implements RouterService {


    public RouterServiceRedisImpl() {
        addServer();
        register();
    }

    /**
     * 添加一个服务器  ip:port
     */
    private static void addServer() {
        RedisUtil.addToSet(REMOTESERVERSETKEY, serverHostPort);
    }

    @Override
    public void register() {
        RSet<String> set = getServers();
        Iterator<String> iter = set.iterator();
        if (set != null && set.size() > 0) {
            while (iter.hasNext()) {
                String server = iter.next();
                if (server != null) {
                    DistributedServerUtil.connectRemoteServer(server);
                }
            }
        }
    }

    /**
     * 获取当前服务器列表
     */
    @Override
    public RSet<String> getServers() {
        return RedisUtil.getSet(REMOTESERVERSETKEY);
    }

    @Override
    public void addClient(String clientId) {
        RedisUtil.setStr(String.format(CLIENTPREFIX, clientId), serverHostPort);
    }

    @Override
    public void removeClient(String clientId) {
        RedisUtil.removeStr(String.format(CLIENTPREFIX, clientId));
    }

    @Override
    public String findServer(String clientId) {
        return RedisUtil.getStr(String.format(CLIENTPREFIX, clientId));
    }

}
