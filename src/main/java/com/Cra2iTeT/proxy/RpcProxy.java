package com.Cra2iTeT.proxy;

import com.Cra2iTeT.annotation.EnableRpc;
import com.Cra2iTeT.annotation.RpcClient;
import com.Cra2iTeT.annotation.RpcMapping;
import com.Cra2iTeT.client.Client;
import com.Cra2iTeT.message.RpcRequestMessage;
import com.Cra2iTeT.utils.SequenceIdGenerator;
import com.alibaba.fastjson.JSON;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * @author Cra2iTeT
 * @since 2023/1/6 16:27
 */
@Slf4j
public class RpcProxy {
    private final Client client;

    private final Class<?> clazz;

    public RpcProxy(Client client, Class<?> clazz) {
        if (!clazz.isAnnotationPresent(RpcClient.class)) {
            throw new RuntimeException("该服务未向Rpc注册");
        }
        this.client = client;
        this.clazz = clazz;
    }

    public <T> T getProxyService() {
        Object o = Proxy.newProxyInstance(this.clazz.getClassLoader(), new Class<?>[]{this.clazz},
                (proxy, method, args) -> {
                    if (!method.isAnnotationPresent(RpcMapping.class)) {
                        throw new RuntimeException("该方法未向Rpc注册");
                    }
                    // 1. 将方法调用转换为 消息对象
                    int sequenceId = SequenceIdGenerator.nextId();
                    RpcRequestMessage msg = new RpcRequestMessage(
                            sequenceId,
                            clazz.getName(),
                            Client.clazz.getAnnotation(EnableRpc.class).value(),
                            clazz.getAnnotation(RpcClient.class).value(),
                            method.getAnnotation(RpcMapping.class).value(),
                            method.getName(),
                            method.getReturnType(),
                            method.getParameterTypes(),
                            args
                    );
                    // 2. 准备一个空 Promise 对象，来接收结果 存入集合 指定 promise 对象异步接收结果线程
                    DefaultPromise<Object> promise = new DefaultPromise<>(Client.group.next());
                    Client.PROMISES.put(sequenceId, promise);
                    // 3. 将消息对象发送出去
                    client.sendRpcRequest(msg);
                    // 4. 等待 promise 结果
                    log.debug("/////////////////////////////////////");
                    promise.await();
                    if (promise.isSuccess()) {
                        // 调用正常
                        log.debug("接收端接收到的结果,{}", JSON.parseObject((String) promise.getNow(), method.getReturnType()));
                        return JSON.parseObject((String) promise.getNow(), method.getReturnType());
                    } else {
                        // 调用失败
                        throw new RuntimeException(promise.cause());
                    }
                });
        return (T) o;
    }
}
