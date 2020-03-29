package com.cwj.mvn.framework.http;

import com.cwj.mvn.framework.socket.AbstractClientService;
import com.cwj.mvn.framework.socket.AbstractOperationChain;

public class MClientService extends AbstractClientService<String> {

    public MClientService(AbstractOperationChain<String> handlers) {
        super(handlers);
    }
}
