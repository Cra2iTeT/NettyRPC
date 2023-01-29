package com.Cra2iTeT.message;

import lombok.Getter;
import lombok.ToString;

/**
 *
 */
@Getter
@ToString(callSuper = true)
public class RpcRequestMessage extends Message {
    /**
     * 实例对象在Server注册的应用名
     */
    private String name;

    /**
     * 调用者在Server注册的应用名
     */
    private String from;

    /**
     * 方法映射名
     */
    private String value;

    /**
     * 调用的接口全限定名，服务端根据它找到实现
     */
    private String interfaceName;
    /**
     * 调用接口中的方法名
     */
    private String methodName;
    /**
     * 方法返回类型
     */
    private Class<?> returnType;
    /**
     * 方法参数类型数组
     */
    private Class<?>[] parameterTypes;
    /**
     * 方法参数值数组
     */
    private Object[] parameterValue;

    public RpcRequestMessage(int sequenceId,
                             String interfaceName,
                             String from,
                             String name,
                             String value,
                             String methodName,
                             Class<?> returnType,
                             Class[] parameterTypes,
                             Object[] parameterValue) {
        super.setSequenceId(sequenceId);
        this.interfaceName = interfaceName;
        this.value = value;
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.parameterValue = parameterValue;
        this.name = name;
        this.from = from;
    }

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_REQUEST;
    }
}
