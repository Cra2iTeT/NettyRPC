package com.Cra2iTeT.client;

import com.Cra2iTeT.annotation.EnableRpc;
import com.Cra2iTeT.annotation.RpcScan;
import com.Cra2iTeT.proxy.RpcProxy;
import com.Cra2iTeT.service.HelloService;

/**
 * @author Cra2iTeT
 * @since 2023/1/6 15:49
 */
@RpcScan("com.Cra2iTeT.service")
@EnableRpc("testService")
public class ClientStart {
    public static void main(String[] args) {
        Client client = new Client("localhost", 8080, ClientStart.class);
//        HelloService helloService = new RpcProxy(client, HelloService.class).getProxyService();
//        System.out.println(helloService.sayHello("Cra2iTeT"));
    }
}
