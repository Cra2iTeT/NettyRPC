package com.Cra2iTeT.client;

import com.Cra2iTeT.annotation.EnableRpc;
import com.Cra2iTeT.annotation.RpcScan;
import com.Cra2iTeT.annotation.RpcMapping;
import com.Cra2iTeT.annotation.RpcService;
import com.Cra2iTeT.factory.ServiceFactory;
import com.Cra2iTeT.handler.RpcRequestMessageHandler;
import com.Cra2iTeT.handler.RpcResponseMessageHandler;
import com.Cra2iTeT.message.RpcRegistryMessage;
import com.Cra2iTeT.message.RpcRequestMessage;
import com.Cra2iTeT.prototol.MessageCodecSharable;
import com.Cra2iTeT.prototol.ProcotolFrameDecoder;
import com.Cra2iTeT.server.Server;
import com.Cra2iTeT.utils.SequenceIdGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Cra2iTeT
 * @since 2023/1/6 15:49
 */
@Slf4j
@Getter
public class Client {
    private static String host;
    private static Integer port;
    private static Bootstrap bootstrap;
    public static NioEventLoopGroup group;
    private static Map<String, Channel> channels;
    public static ServiceFactory serviceFactory;
    public static Class<?> clazz;
    public static Map<Integer, Promise<Object>> PROMISES;
    public static Boolean isRegistry;

    public Client(String host, Integer port, Class<?> clazz) {
        Client.host = host;
        Client.port = port;
        Client.clazz = clazz;
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
        channels = new ConcurrentHashMap<>();
        PROMISES = new ConcurrentHashMap<>();
        serviceFactory = new ServiceFactory();
        isRegistry = false;
        Init();
        registry();
    }

    private void Init() {
        LoggingHandler loggingHandler = new LoggingHandler();
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        RpcRequestMessageHandler RPC_REQUEST_HANDLER = new RpcRequestMessageHandler();
        bootstrap
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline().addLast(loggingHandler);
                        sc.pipeline().addLast(new ProcotolFrameDecoder());
                        sc.pipeline().addLast(messageCodecSharable);
                        sc.pipeline().addLast(RPC_HANDLER);
                        sc.pipeline().addLast(RPC_REQUEST_HANDLER);
                    }
                });
    }

    private void registry() {
        if (!clazz.isAnnotationPresent(RpcScan.class)) {
            throw new RuntimeException("??????????????????????????????");
        }
        RpcScan rpcScanAnnotation = clazz.getDeclaredAnnotation(RpcScan.class);
        String registryName = clazz.getAnnotation(EnableRpc.class).value();
        // com.Cra2iTeT.xx
        String path = rpcScanAnnotation.value();
        String classPath = path.replaceAll("\\.", "/");
        // com/Cra2iTeT/xx
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(classPath);
        if (resource == null) {
            throw new RuntimeException("??????????????????????????????");
        }
        File directory = new File(resource.getFile());
        // ????????????????????????
        if (!directory.isDirectory()) {
            throw new RuntimeException("??????????????????????????????");
        }
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            String fileName = file.getName();
            // ??????class??????????????????
            if (!fileName.endsWith(".class")) {
                continue;
            }
            // ????????????
            classPath = path + "." + fileName.substring(0, fileName.length() - 6);
            try {
                Class<?> clazz = classLoader.loadClass(classPath);
                if (clazz.isInterface()) {
                    continue;
                }
                // ????????????RpcService????????????
                if (clazz.isAnnotationPresent(RpcService.class)) {
                    String beanName = classPath.substring(0, classPath.length() - 4);
                    Object object = clazz.newInstance();
                    for (Method method : clazz.getMethods()) {
                        if (method.isAnnotationPresent(RpcMapping.class)) {
                            String methodName = method.getAnnotation(RpcMapping.class).value();
                            if ("".equals(methodName)) {
                                methodName = method.getName();
                            }
                            add2ServiceFactory(beanName, object, methodName, method);
                            if (!isRegistry) {
                                registry2Server(registryName);
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("???????????????");
            }
        }
    }

    /**
     * ????????????
     *
     * @param beanName
     * @param object
     * @param value
     * @param method
     */
    private void add2ServiceFactory(String beanName, Object object, String value, Method method) {
        log.debug("?????????method,{}", value);
        serviceFactory.addMethod(beanName, object, value, method);
    }

    /**
     * ??????????????????
     *
     * @param registryName
     */
    private void registry2Server(String registryName) {
        try {
            // TODO ?????????????????????????????????????????????channel
            InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
            Channel channel = getChannel(inetSocketAddress);
            if (!channel.isActive() || !channel.isRegistered()) {
                group.shutdownGracefully();
                throw new RuntimeException("??????????????????????????????");
            }
            RpcRegistryMessage msg = new RpcRegistryMessage(SequenceIdGenerator.nextId(), registryName);
            DefaultPromise<Object> promise = new DefaultPromise<>(Client.group.next());
            Client.PROMISES.put(msg.getSequenceId(), promise);
            channel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.debug("????????????????????????");
                }
            });
            promise.await();
            if (promise.isSuccess()) {
                // ????????????
                log.debug(String.valueOf(Client.isRegistry));
                Client.isRegistry = (Boolean) promise.getNow();
                log.debug(String.valueOf(Client.isRegistry));
            } else {
                // ????????????
                throw new RuntimeException(promise.cause());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("????????????????????????");
        }
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = null;
        String key = inetSocketAddress.toString();
        if (channels.containsKey(key)) {
            channel = channels.get(key);
            if (channel.isActive()) {
                return channel;
            }
            channels.remove(key);
        }
        try {
            channel = bootstrap.connect(inetSocketAddress).sync().channel();
            channel.closeFuture().addListener(future -> log.debug("????????????"));
            channels.put(inetSocketAddress.toString(), channel);
            return channel;
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.debug("????????????");
        }
        return channel;
    }

    public void sendRpcRequest(RpcRequestMessage msg) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        Channel channel = getChannel(inetSocketAddress);
        if (!channel.isActive() || !channel.isRegistered()) {
            group.shutdownGracefully();
            throw new RuntimeException("??????????????????");
        }
        channel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.debug("???????????????????????????");
            }
        });
    }
}
