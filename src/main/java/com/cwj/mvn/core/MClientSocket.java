package com.cwj.mvn.core;

import java.io.IOException;

import com.cwj.mvn.framework.socket.AbstractClientSocket;
import com.cwj.mvn.utils.ByteUtils;

public class MClientSocket extends AbstractClientSocket<byte[]> {
    
    private static final byte[] REQUEST_END = {13, 10, 13, 10};

    protected MClientSocket(ClientBuilder builder) throws IOException {
        super(builder);
    }
    
    @Override
    protected byte[] encrypt(byte[] message) {
        return message;
    }
    
    public MClientSocket() throws IOException {
        super(null);
    }
    
    @Override
    protected int positionMessage(byte[] buffer) {
        int pos = ByteUtils.indexOf(buffer, REQUEST_END);
        if (pos == -1) return 0;
        return pos + 4;
    }

    @Override
    protected byte[] parseMessage(byte[] buffer, int length) {
        byte[] msg = new byte[length];
        System.arraycopy(buffer, 0, msg, 0, length);
        log.info("Receive message = {}", new String(msg));
        return msg;
    }
    
    @Override
    public void afterSend(byte[] message) {
        log.info("Send to message = {}", new String(message));
    }
    
    @Override
    public void sendError(byte[] message, Throwable e) {
        log.error("Send to error, message = " + new String(message), e);
    }
}
