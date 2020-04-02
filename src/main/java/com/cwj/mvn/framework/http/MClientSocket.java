package com.cwj.mvn.framework.http;

import java.io.IOException;

import com.cwj.mvn.framework.socket.AbstractClientSocket;

public class MClientSocket extends AbstractClientSocket<byte[]> {
    
    private static final byte[] REQUEST_END = {13, 10, 13, 10};

    protected MClientSocket(ClientBuilder builder) throws IOException {
        super(builder);
    }
    
    @Override
    protected byte[] encrypt(byte[] message) {
        return message;
    }

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
    protected byte[] parseMessage(byte[] buffer, int length) {
        byte[] msg = new byte[length];
        System.arraycopy(buffer, 0, msg, 0, length);
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
