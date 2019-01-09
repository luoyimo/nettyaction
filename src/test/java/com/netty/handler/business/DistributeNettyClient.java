package com.netty.handler.business;

import com.netty.distribute.DistributedServerClient;

public class DistributeNettyClient {
    public static void main(String[] args) throws Exception {


        new DistributedServerClient("127.0.0.1:8999").connect();

    }
}