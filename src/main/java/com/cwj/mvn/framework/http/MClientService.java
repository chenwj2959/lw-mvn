package com.cwj.mvn.framework.http;

import com.cwj.mvn.framework.socket.AbstractClientService;
import com.cwj.mvn.framework.socket.AbstractOperationChain;

public class MClientService extends AbstractClientService<byte[]> {

    public MClientService(AbstractOperationChain<byte[]> handlers) {
        super(handlers);
    }
}
