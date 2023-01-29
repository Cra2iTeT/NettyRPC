package com.Cra2iTeT.handler;


import com.Cra2iTeT.message.RpcRegistryMessage;
import com.Cra2iTeT.message.RpcResponseMessage;
import com.Cra2iTeT.server.Server;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Rpc请求处理器
 *
 * @author chenlei
 */
@Slf4j
@ChannelHandler.Sharable
public class ServerRpcRegistryMessageHandler extends SimpleChannelInboundHandler<RpcRegistryMessage> {

    /**
     * 读空闲
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("长时间未收到心跳包，断开连接...");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext chc, RpcRegistryMessage rpcRegistryMessage) {
        RpcResponseMessage responseMessage = new RpcResponseMessage();
        responseMessage.setSequenceId(rpcRegistryMessage.getSequenceId());
        try {
            String name = rpcRegistryMessage.getName();
            Channel channel = chc.channel();
            Server.REGISTRY_FACTORY.put(name, channel);
            responseMessage.setReturnValue(true);
        } finally {
            chc.writeAndFlush(responseMessage);
            ReferenceCountUtil.release(rpcRegistryMessage);
        }
    }
}
