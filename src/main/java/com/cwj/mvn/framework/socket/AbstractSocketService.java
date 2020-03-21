package com.cwj.mvn.framework.socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import com.cwj.mvn.framework.BaseRunnable;
import com.cwj.mvn.framework.ThreadPool;

public abstract class AbstractSocketService<T> extends BaseRunnable {
    
    private final HashMap<String, AbstractClientSocket<T>> clientMap;

    private ServerSocket server;

    // total client size
    private static int size = 0;

    public AbstractSocketService(String tag, int port) throws Exception {
        super(tag);
        server = new ServerSocket(port);
        clientMap = new HashMap<>();
    }

    @Override
    public void loop() {
        try {
            int errorNum = 0;
            while (run) {
                try {
                    log.info("Server socket wait clien connect");
                    Socket client = server.accept();
                    size++;
                    log.info("Has new client socket connect!");
                    String tag = createTag();
                    AbstractClientSocket<T> socket = createClientSocket(tag, client);
                    AbstractClientService<T> service = createClientService(socket);
                    ThreadPool.putServer(service);
                    clientMap.put(tag, socket);
                } catch (Exception e) {
                    if (e instanceof SocketException && "Socket closed".equals(e.getMessage())) {
                        break;
                    }
                    log.error("Accept new client socket happen some error", e);
                    if (++errorNum > 5) break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            close();
        }
    }

    public int size() {
        return size;
    }

    /**
     * 对所有的client发送广播消息
     * @return 发送消息的数量
     */
    public int broadcast(T message) {
        int ans = 0;
        for (Map.Entry<String, AbstractClientSocket<T>> clients : clientMap.entrySet()) {
            AbstractClientSocket<T> client = clients.getValue();
            if (!client.send(message)) {
                log.error("Broadcast to client failed, client = " + client.TAG);
                client.close();
                continue;
            }
            ans++;
        }
        return ans;
    }
    
    /** 发送消息给某个Client */
    public void sendToClient(String tag, T message) {
        AbstractClientSocket<T> client = clientMap.get(tag);
        if (client == null) {
            log.error("Send message to client failed, Not found " + tag + " client");
            return;
        }
        client.send(message);
    }

    public HashMap<String, AbstractClientSocket<T>> getClients() {
        return clientMap;
    }

    @Override
    public void close() {
        super.close();
        try {
            if (server != null) {
                server.close();
                server = null;
            }
        } catch (Exception e) {
            log.error("Close socket service failed", e);
            Thread.currentThread().interrupt();
        }
    }

    public abstract String createTag();
    
    public abstract AbstractClientSocket<T> createClientSocket(String tag, Socket socket) throws Exception;
    
    public abstract AbstractClientService<T> createClientService(AbstractClientSocket<T> client);
}