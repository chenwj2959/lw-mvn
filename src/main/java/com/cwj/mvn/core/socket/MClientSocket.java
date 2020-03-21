package com.cwj.mvn.core.socket;

import java.io.IOException;

import com.cwj.mvn.framework.http.HttpRequest;
import com.cwj.mvn.framework.socket.AbstractClientSocket;

public class MClientSocket extends AbstractClientSocket<HttpRequest> {

    protected MClientSocket(ClientBuilder builder) throws IOException {
        super(builder);
    }

    @Override
    protected byte[] encrypt(HttpRequest message) {
        return null;
    }

    @Override
    protected int positionMessage(byte[] buffer) {
        return 0;
    }

    @Override
    protected HttpRequest parseMessage(byte[] buffer, int length) {
        return null;
    }

}
