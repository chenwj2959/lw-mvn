package com.cwj.mvn.framework.socket;

import java.util.HashMap;

import com.cwj.mvn.framework.socket.chain.AbstractHandler;

public abstract class AbstractOperation<T> extends AbstractHandler<T, Boolean> {

    @SuppressWarnings("unchecked")
    @Override
    public Boolean handle(T message, HashMap<String, Object> paramMap) throws Exception {
        return handle(message, paramMap, (AbstractClientSocket<T>) paramMap.get(AbstractOperationChain.CLIENT));
    }
    
    public abstract Boolean handle(T message, HashMap<String, Object> paramMap, AbstractClientSocket<T> client);
}
