package com.cwj.mvn.framework.socket;

import com.orhanobut.logger.Logger;
import com.sound.bridge.phase2.framework.BaseRunnable;

public abstract class BridgeClientService<T> extends BaseRunnable {
    
    protected AbstractClientSocket<T> client;
    
    protected AbstractOperationChain<T> handlers;

    protected static final int MAX_TIMEOUT_NUMBER = 2;

    public BridgeClientService(AbstractClientSocket<T> client, AbstractOperationChain<T> handlers) {
        super(client.TAG, client.threadName);
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
                    Logger.e("Do handle message has error, message = {}", message.toString());
                }
            } catch (Exception e) {
                Logger.e(e, e.getMessage());
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