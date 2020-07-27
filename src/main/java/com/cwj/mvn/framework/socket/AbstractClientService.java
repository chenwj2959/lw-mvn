package com.cwj.mvn.framework.socket;

import java.net.SocketException;

import com.cwj.mvn.framework.BaseRunnable;

public abstract class AbstractClientService<T> extends BaseRunnable {
    
    protected AbstractClientSocket<T> client;
    
    protected AbstractOperationChain<T> handlers;

    protected static final int MAX_TIMEOUT_NUMBER = 2;
    
    public AbstractClientService(AbstractOperationChain<T> handlers) {
        this(handlers.getClient(), handlers);
    }

    private AbstractClientService(AbstractClientSocket<T> client, AbstractOperationChain<T> handlers) {
        super(client.TAG);
        this.client = client;
        this.handlers = handlers;
    }
    
    @Override
    public void loop() {
        int timeoutNum = 0;
        while (run && !client.closed()) {
            try {
                T message = client.receive();
                if (message == null) {
                    if (timeoutNum++ >= MAX_TIMEOUT_NUMBER) break;
                    continue;
                }
                beforeHandle(message);
                if (!handlers.doHanlde(message)) {
                    log.error("Do handle message has error, message = {}", message);
                }
            } catch (SocketException e) {
                log.error(e.getMessage(), e);
                break;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void beforeHandle(T message) {}

    public AbstractClientSocket<T> getClient() {
        return this.client;
    }

    public AbstractOperationChain<T> getHandlers() {
        return handlers;
    }

    @Override
    public void close() {
        if (closed()) return;
        this.run = false;
        client.close();
    }
}