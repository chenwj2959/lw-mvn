package com.cwj.mvn.framework.socket;

import com.orhanobut.logger.Logger;
import com.sound.bridge.R;
import com.sound.bridge.core.pool.ThreadPool;
import com.sound.bridge.core.pool.config.ActivityResources;
import com.sound.bridge.framework.BaseRunnable;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

public abstract class BridgeSocketService<T> extends BaseRunnable {
    
    private static final char[] SERVER_KEY_PASSWORD = "server".toCharArray();
    private static final String SERVER_AGREEMENT = "SSL";// 使用协议
    private static final String SERVER_KEY_MANAGER = "X509";// 密钥管理器
    private static final String SERVER_KEY_KEYSTORE = "BKS";// 密库，这里用的是Android自带密库
    
    private final HashMap<String, AbstractClientSocket<T>> clientMap;

    private ServerSocket server;

    // total client size
    private static int size = 0;

    public BridgeSocketService(String tag, int port) throws Exception {
        this(tag, port, false);
    }

    public BridgeSocketService(String tag, int port, boolean ssl) throws Exception {
        super(tag);
        server = createServerSocket(port, ssl);
        clientMap = new HashMap<>();
    }

    @Override
    public void loop() {
        try {
            ThreadPool pool = ThreadPool.getInstance();
            int errorNum = 0;
            while (run) {
                try {
                    Logger.i("Server socket wait clien connect");
                    Socket client = server.accept();
                    size++;
                    Logger.i("Has new client socket connect!");
                    String tag = createTag();
                    AbstractClientSocket<T> socket = createClientSocket(tag, client);
                    BridgeClientService<T> service = createClientService(socket);
                    pool.putServer(service);
                    clientMap.put(tag, socket);
                } catch (Exception e) {
                    if (e instanceof SocketException && "Socket closed".equals(e.getMessage())) {
                        break;
                    }
                    Logger.e(e, "Accept new client socket happen some error");
                    if (++errorNum > 5) break;
                }
            }
        } catch (Exception e) {
            Logger.e(e, e.getMessage());
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
                Logger.e("Broadcast to client failed, client = " + client.TAG);
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
            Logger.e("Send message to client failed, Not found " + tag + " client");
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
            Logger.e(e, "Close socket service failed");
            Thread.currentThread().interrupt();
        }
    }

    public abstract String createTag();
    
    public abstract AbstractClientSocket<T> createClientSocket(String tag, Socket socket) throws Exception;
    
    public abstract BridgeClientService<T> createClientService(AbstractClientSocket<T> client);
    
    private ServerSocket createServerSocket(int port, boolean ssl) throws Exception {
        if (ssl) {
            // 取得SSLContext
            SSLContext ctx = SSLContext.getInstance(SERVER_AGREEMENT);
            // 取得SunX509私钥管理器
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(SERVER_KEY_MANAGER);
            // 取得JKS密库实例
            KeyStore ks = KeyStore.getInstance(SERVER_KEY_KEYSTORE);
            // 加载服务端私钥
            ks.load(ActivityResources.getInstance().openRawResource(R.raw.server), SERVER_KEY_PASSWORD);
            // 初始化
            kmf.init(ks, SERVER_KEY_PASSWORD);
            // 初始化SSLContext
            ctx.init(kmf.getKeyManagers(), null, null);
            // 通过SSLContext取得ServerSocketFactory，创建ServerSocket
            return ctx.getServerSocketFactory().createServerSocket(port);
        } else return new ServerSocket(port);
    }
}