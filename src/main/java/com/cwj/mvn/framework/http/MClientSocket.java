package com.cwj.mvn.framework.http;

import java.io.IOException;

import com.cwj.mvn.constant.Constant;
import com.cwj.mvn.framework.socket.AbstractClientSocket;

public class MClientSocket extends AbstractClientSocket<String> {
    
    private static final byte[] REQUEST_END = {13, 10, 13, 10};

    protected MClientSocket(ClientBuilder builder) throws IOException {
        super(builder);
    }

    @Override
    protected byte[] encrypt(String message) {
        return message.getBytes(Constant.UTF8);
    }

    /**
     * http结束标识为13 10 13 10
     */
    @Override
    protected int positionMessage(byte[] buffer) {
        int len = buffer.length;
        if (len < 4)  return 0;
        byte first = REQUEST_END[0];
        int max = len - 4;
        for (int i = 0; i < len; i++) {
            if (buffer[i] != first) {
                while (++i <= max && buffer[i] != first);
            }
            if (i < max) {
                int j = i + 1;
                int end = i + 4;
                for (int k = 1; j < end && REQUEST_END[k] == buffer[j]; k++, j++);
                if (j == end) return i;
            }
        }
        return 0;
    }

    @Override
    protected String parseMessage(byte[] buffer, int length) {
        return new String(buffer, 0, length, Constant.UTF8);
    }

    @Override
    public void afterSend(String message) {
        log.info("Return response = {}", message);
    }
    
    @Override
    public void sendError(String message, Throwable e) {
        log.error("Return response error, message = " + message, e);
    }
}
