package com.Cra2iTeT.server;

import com.Cra2iTeT.handler.ServerRpcRegistryMessageHandler;
import com.Cra2iTeT.handler.RpcResponseMessageHandler;
import com.Cra2iTeT.handler.ServerRpcRequestMessageHandler;
import com.Cra2iTeT.handler.ServerRpcResponseMessageHandler;
import com.Cra2iTeT.prototol.MessageCodecSharable;
import com.Cra2iTeT.prototol.ProcotolFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Cra2iTeT
 * @since 2023/1/6 15:49
 */
@Slf4j
public class Server {
    private final String host;
    private final Integer port;
    private final ServerBootstrap serverBootstrap;
    private final NioEventLoopGroup boss;
    public static NioEventLoopGroup worker;
    public static Map<String, Channel> REGISTRY_FACTORY = new ConcurrentHashMap<>();
    public static Map<Integer, Promise<Object>> PROMISES;

    public Server(String host, Integer port) {
        this.host = host;
        this.port = port;
        serverBootstrap = new ServerBootstrap();
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        PROMISES = new ConcurrentHashMap<>();
    }

    public void run() {
        LoggingHandler loggingHandler = new LoggingHandler();
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        ServerRpcRequestMessageHandler RPC_HANDLER = new ServerRpcRequestMessageHandler();
        ServerRpcRegistryMessageHandler RPC_REGISTRY_HANDLER = new ServerRpcRegistryMessageHandler();
        ServerRpcResponseMessageHandler RPC_RESP_HANDLER = new ServerRpcResponseMessageHandler();
        try {
            serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            sc.pipeline().addLast(loggingHandler);
                            sc.pipeline().addLast(new ProcotolFrameDecoder());
                            sc.pipeline().addLast(messageCodecSharable);
                            sc.pipeline().addLast(RPC_REGISTRY_HANDLER);
                            sc.pipeline().addLast(RPC_HANDLER);
                            sc.pipeline().addLast(RPC_RESP_HANDLER);
                        }
                    });
            Channel channel = serverBootstrap.bind(port).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}
