package com.Cra2iTeT.handler;


import com.Cra2iTeT.client.Client;
import com.Cra2iTeT.message.RpcRequestMessage;
import com.Cra2iTeT.message.RpcResponseMessage;
import com.Cra2iTeT.server.Server;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务器转发请求处理器
 */
@Slf4j
@ChannelHandler.Sharable
public class ServerRpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    /**
     * 读事件
     *
     * @param ctx
     * @param message
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage message) {
        RpcResponseMessage responseMessage = new RpcResponseMessage();
        //设置请求的序号
        responseMessage.setSequenceId(message.getSequenceId());
        responseMessage.setFrom(message.getFrom());
        responseMessage.setTo(message.getName());
        responseMessage.setReturnValue(message.getReturnType());
        responseMessage.setReturnValue(message.getReturnType());
        try {
            // 找到实例通道
            Channel channel = Server.REGISTRY_FACTORY.get(responseMessage.getTo());
            // 2. 准备一个空 Promise 对象，来接收结果 存入集合 指定 promise 对象异步接收结果线程
            DefaultPromise<Object> promise = new DefaultPromise<>(Server.worker.next());
            Server.PROMISES.put(message.getSequenceId(), promise);
            // 3. 将消息对象发送出去
            if (!channel.isActive() || !channel.isRegistered()) {
                Server.REGISTRY_FACTORY.remove(message.getName());
                throw new RuntimeException("不存在指定" + message.getName() + "服务");
            }
            channel.writeAndFlush(message).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.debug("服务器端转发消息成功");
                }
            });
        } finally {
            ReferenceCountUtil.release(message);
        }
    }

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
}
