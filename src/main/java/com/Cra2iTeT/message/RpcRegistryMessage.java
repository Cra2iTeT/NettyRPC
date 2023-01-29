package com.Cra2iTeT.message;

import com.Cra2iTeT.factory.ServiceFactory;
import lombok.Getter;
import lombok.ToString;

/**
 *
 */
@Getter
@ToString(callSuper = true)
public class RpcRegistryMessage extends Message {
    private final String name;

    public RpcRegistryMessage(int sequenceId, String name) {
        super.setSequenceId(sequenceId);
        this.name = name;
    }

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_REQUEST;
    }
}
