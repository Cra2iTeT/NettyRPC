package com.Cra2iTeT.server;

import com.Cra2iTeT.annotation.RpcServer;

/**
 * @author Cra2iTeT
 * @since 2023/1/6 15:49
 */
@RpcServer
public class ServerStart {
    public static void main(String[] args) {
        new Server("localhost",8080).run();
    }
}
