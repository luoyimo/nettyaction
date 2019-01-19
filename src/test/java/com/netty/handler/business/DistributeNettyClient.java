package com.netty.handler.business;


public class DistributeNettyClient {
    public static void main(String[] args) throws Exception {


        new DistributedServerClient("127.0.0.1:8099").connect();

    }
}