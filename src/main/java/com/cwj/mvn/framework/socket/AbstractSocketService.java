package com.cwj.mvn.framework.socket;

import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.cwj.mvn.framework.BaseRunnable;
import com.cwj.mvn.framework.ThreadPool;

public abstract class AbstractSocketService<T> extends BaseRunnable {
    
    private static final String SERVER_AGREEMENT = "SSL";// 使用协议
    private static final String SERVER_KEY_MANAGER = "SunX509";// 密钥管理器
    private static final String SERVER_KEY_KEYSTORE = "JKS";// 密库
    
    private final HashMap<String, AbstractClientSocket<T>> clientMap;

    private ServerSocket server;
    // total client size
    private static int size = 0;
    
    private String certificatePath;
    private String certificatePassword;
    
    public AbstractSocketService(String tag, int port) throws Exception {
        this(tag, port, false, null, null);
    }

    public AbstractSocketService(String tag, int port, boolean ssl, String cpath, String cpassword) throws Exception {
        super(tag);
        server = createServerSocket(port, ssl);
        clientMap = new HashMap<>();
        this.certificatePath = cpath;
        this.certificatePassword = cpassword;
    }

    @Override
    public void loop() {
        try {
            int errorNum = 0;
            while (run) {
                try {
                    log.info("Server socket wait clien connect");
                    Socket client = server.accept();
                    if (size++ > 9999) size = 1;
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
    
    private ServerSocket createServerSocket(int port, boolean ssl) throws Exception {
        if (ssl) {
            try (FileInputStream fis = new FileInputStream(certificatePath)) {
             // 取得SSLContext
                SSLContext ctx = SSLContext.getInstance(SERVER_AGREEMENT);
                // 取得SunX509私钥管理器
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(SERVER_KEY_MANAGER);
                // 取得JKS密库实例
                KeyStore ks = KeyStore.getInstance(SERVER_KEY_KEYSTORE);
                // 加载服务端私钥
                char[] pwd = certificatePassword.toCharArray();
                ks.load(fis, pwd);
                // 初始化
                kmf.init(ks, pwd);
                // 初始化SSLContext
                ctx.init(kmf.getKeyManagers(), null, null);
                // 通过SSLContext取得ServerSocketFactory，创建ServerSocket
                return ctx.getServerSocketFactory().createServerSocket(port);
            }
        } else return new ServerSocket(port);
    }
}