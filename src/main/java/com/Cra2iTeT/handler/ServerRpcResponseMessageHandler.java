package com.Cra2iTeT.handler;

import com.Cra2iTeT.client.Client;
import com.Cra2iTeT.message.RpcResponseMessage;
import com.Cra2iTeT.server.Server;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理RpcResponse相应的处理器
 *
 * @author chenlei
 */
@Slf4j
@ChannelHandler.Sharable
public class ServerRpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        try {
            log.debug("{}", msg);
            // 每次使用完都要移除
            RpcResponseMessage message = new RpcResponseMessage();
            message.setSequenceId(msg.getSequenceId());
            message.setReturnValue(msg.getReturnValue());
            message.setFrom(msg.getFrom());
            message.setTo(msg.getTo());
            message.setReturnType(msg.getReturnType());
            Channel channel = Server.REGISTRY_FACTORY.get(message.getFrom());
            if (!channel.isActive() || !channel.isRegistered()) {
                Server.REGISTRY_FACTORY.remove(message.getFrom());
                throw new RuntimeException("不存在指定" + message.getFrom() + "服务");
            }
            channel.writeAndFlush(message).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.debug("服务器端转发结果成功");
                }
            });
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("出现异常" + cause);
        ctx.close();
    }
}
