package com.Cra2iTeT.message;

import lombok.Getter;
import lombok.ToString;

/**
 *
 */
@Getter
@ToString(callSuper = true)
public class RpcRespMessage extends Message {
    private final Boolean isRegistry;

    public RpcRespMessage(int sequenceId, Boolean isRegistry) {
        super.setSequenceId(sequenceId);
        this.isRegistry = isRegistry;
    }

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_REQUEST;
    }
}
