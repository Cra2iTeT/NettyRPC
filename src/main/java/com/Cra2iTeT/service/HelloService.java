package com.Cra2iTeT.service;

import com.Cra2iTeT.annotation.RpcClient;
import com.Cra2iTeT.annotation.RpcMapping;

@RpcClient("testService")
public interface HelloService {
    @RpcMapping("sayHello")
    String sayHello(String name);

    @RpcMapping("test")
    Test test(String name);

    @RpcMapping("hi")
    void hi(String ss);
}
