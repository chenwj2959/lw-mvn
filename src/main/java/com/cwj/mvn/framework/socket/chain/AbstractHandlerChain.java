package com.cwj.mvn.framework.socket.chain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHandlerChain<T, K> {
    
    protected static final Logger log = LoggerFactory.getLogger(AbstractHandlerChain.class);

    private AbstractHandler<T, K> firstHandler;
    
    private HashMap<String, Object> paramMap;

    @SafeVarargs
    public AbstractHandlerChain(Class<? extends AbstractHandler<T, K>>... handlers) throws Exception {
        Objects.requireNonNull(handlers, "Get responsibility chain failed, handlers cannot be null");
        if (handlers.length == 0) throw new NullPointerException("Get responsibility chain failed, handlers length must be more than 0");
        List<AbstractHandler<T, K>> list = new ArrayList<>(handlers.length);
        for (Class<? extends  AbstractHandler<T, K>> handler : handlers) {
            AbstractHandler<T, K> newHandler = handler.newInstance();
            list.add(newHandler);
        }
        
        for (int i = 1; i < list.size(); i++) {
            list.get(i - 1).setNextHandler(list.get(i));
        }
        this.firstHandler = list.get(0);
        this.paramMap = new HashMap<>();
    }

    public K doHanlde(T message) throws Exception {
        if (firstHandler == null) return null;
        else return firstHandler.handle(message, paramMap);
    }
    
    public void put(String key, Object value) {
        paramMap.put(key, value);
    }
    
    public void putAll(Map<String, Object> params) {
        if (params == null || params.size() == 0) return;
        paramMap.putAll(params);
    }
    
    public Object get(String key) {
        return paramMap.get(key);
    }
    
    public HashMap<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(HashMap<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public AbstractHandler<T, K> getFirstHandler() {
        return firstHandler;
    }

    public void setFirstHandler(AbstractHandler<T, K> firstHandler) {
        this.firstHandler = firstHandler;
    }
}
