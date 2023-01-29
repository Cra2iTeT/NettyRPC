package com.Cra2iTeT.handler;


import com.Cra2iTeT.client.Client;
import com.Cra2iTeT.factory.ServiceFactory;
import com.Cra2iTeT.message.RpcRequestMessage;
import com.Cra2iTeT.message.RpcResponseMessage;
import com.Cra2iTeT.service.Test;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Rpc请求处理器
 *
 * @author chenlei
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

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
        Object result;
        try {
            //通过名称从工厂获取本地注解了@RpcServer的实例
            Object service = ServiceFactory.getObject(message.getInterfaceName());
            //获取方法 方法名，参数
            Method method = ServiceFactory.getMethod(message.getValue());
            //调用
            result = method.invoke(service, message.getParameterValue());
            //设置返回值
            String test = JSON.toJSONString(result);
            //设置返回值
            log.debug("方法调用成功，结果*****************{}", test);
            responseMessage.setReturnValue(test);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("远程调用出错");
        } finally {
            ctx.writeAndFlush(responseMessage);
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
