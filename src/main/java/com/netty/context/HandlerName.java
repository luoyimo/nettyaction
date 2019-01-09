package com.netty.context;

/**
 * @Author hu
 * @Description: 定义处理器名称
 * @Date Create In 14:32 2019/1/9 0009
 */
public interface HandlerName {
    /**
     * 读取响应超时
     */
    String READTIMEOUT = "readTimeoutHandler";

    /**
     * 请求认证
     */
    String LOGINAUTHREQ = "loginAuthReqHandler";

    /**
     * 心跳
     */
    String HEARTBEAT = "heartBeatHandler";
}
