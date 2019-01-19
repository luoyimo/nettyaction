package com.netty.handler.business;


public class NettyClient1 {
    public static void main(String[] args) throws Exception {
        new Thread(
                () -> {
                    new NettyClient("119.29.163.100:8099", "localhost1").connect();
                }
        ).start();
    }
}