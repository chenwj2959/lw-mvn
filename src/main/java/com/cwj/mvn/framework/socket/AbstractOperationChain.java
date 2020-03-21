package com.cwj.mvn.framework.socket;

import com.cwj.mvn.framework.socket.chain.AbstractHandler;
import com.cwj.mvn.framework.socket.chain.AbstractHandlerChain;

public abstract class AbstractOperationChain<T> extends AbstractHandlerChain<T, Boolean> {
    
    public static final String CLIENT = "Client";
    
    @SafeVarargs
    public AbstractOperationChain(AbstractClientSocket<T> client, Class<? extends AbstractHandler<T, Boolean>>... handlers) throws Exception {
        super(handlers);
        put(CLIENT, client);
    }
    
    @SuppressWarnings("unchecked")
    public AbstractClientSocket<T> getClient() {
        return (AbstractClientSocket<T>) get(CLIENT);
    }

}
