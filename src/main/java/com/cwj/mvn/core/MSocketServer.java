package com.cwj.mvn.core;

import java.net.Socket;

import com.cwj.mvn.constant.Constant;
import com.cwj.mvn.framework.socket.AbstractClientService;
import com.cwj.mvn.framework.socket.AbstractClientSocket;
import com.cwj.mvn.framework.socket.AbstractOperationChain;
import com.cwj.mvn.framework.socket.AbstractSocketService;

public class MSocketServer extends AbstractSocketService<byte[]> {

    public MSocketServer(int port) throws Exception {
        super(Constant.THREAD_LW_MVN, port);
    }

    @Override
    public String createTag() {
        return "Maven Request (#" + size() + ")";
    }

    @Override
    public AbstractClientSocket<byte[]> createClientSocket(String tag, Socket socket) throws Exception {
        return new MClientSocket.ClientBuilder()
                .socket(socket)
                .tag(tag)
                .build(MClientSocket.class);
    }

    @Override
    public AbstractClientService<byte[]> createClientService(AbstractClientSocket<byte[]> client) {
        try {
            AbstractOperationChain<byte[]> handlers = new AbstractOperationChain<byte[]>(client, MClientOperation.class);
            return new MClientService(handlers);
        } catch (Exception e) {
            log.error("Create client service failed!", e);
            client.close();
            return null;
        }
    }

}
