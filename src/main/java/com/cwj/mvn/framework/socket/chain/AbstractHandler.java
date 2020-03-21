package com.cwj.mvn.framework.socket.chain;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHandler<T, K> {
    
    protected static final Logger log = LoggerFactory.getLogger(AbstractHandler.class);
    
    private AbstractHandler<T, K> nextHandler;

    public AbstractHandler<T, K> getNextHandler() {
        return nextHandler;
    }

    public void setNextHandler(AbstractHandler<T, K> nextHandler) {
        this.nextHandler = nextHandler;
    }
    
    public abstract K handle(T message, HashMap<String, Object> paramMap) throws Exception;
    
    public K nextHandler(T message, HashMap<String, Object> paramMap) throws Exception {
        if (nextHandler == null) {
            log.error("Cannot handle message");
            return null;
        }
        else return nextHandler.handle(message, paramMap);
    }
}
