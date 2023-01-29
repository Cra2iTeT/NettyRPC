package com.Cra2iTeT.message;

import lombok.Data;
import lombok.ToString;

/**
 *
 */
@Data
@ToString(callSuper = true)
public class RpcResponseMessage extends Message {
    /**
     * 返回值
     */
    private Object returnValue;
    /**
     * 异常值
     */
    private Exception exceptionValue;
    /**
     * 返回值类型，用于反序列化
     */
    private Class<?> returnType;

    private String from;

    private String to;

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_RESPONSE;
    }
}
