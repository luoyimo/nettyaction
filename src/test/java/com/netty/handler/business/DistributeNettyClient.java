package com.netty.handler.business;


public class DistributeNettyClient {
    public static void main(String[] args) throws Exception {


        new DistributedServerClient("119.29.163.100:8099").connect();

    }
}