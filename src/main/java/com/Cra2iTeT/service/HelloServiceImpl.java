package com.Cra2iTeT.service;

import com.Cra2iTeT.annotation.RpcMapping;
import com.Cra2iTeT.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

@RpcService
@Slf4j
public class HelloServiceImpl implements HelloService {
    @RpcMapping("sayHello")
    @Override
    public String sayHello(String name) {
        return "你好, " + name;
    }

    @RpcMapping("test")
    @Override
    public Test test(String name) {
        return new Test(name);
    }

    @RpcMapping("hi")
    @Override
    public void hi(String ss) {
        log.debug("his");
    }
}
