package com.netty.handler.business;

/**
 * @Author hu
 * @Description:
 * @Date Create In 14:47 2019/1/19 0019
 */
public class NettyClient2 {
    public static void main(String[] args) {
        new Thread(
                () -> {
                    new NettyClient("119.29.163.100:8098", "localhost2").connect();
                }
        ).start();
    }
}
