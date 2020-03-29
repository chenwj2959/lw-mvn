package com.cwj.mvn.framework.http;

import java.util.HashMap;

import com.cwj.mvn.framework.socket.AbstractClientSocket;
import com.cwj.mvn.framework.socket.AbstractOperation;

public class MClientOperation extends AbstractOperation<String> {

    @Override
    public Boolean handle(String message, HashMap<String, Object> paramMap, AbstractClientSocket<String> client) {
        return null;
    }
}
