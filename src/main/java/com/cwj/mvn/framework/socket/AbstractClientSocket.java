package com.cwj.mvn.framework.socket;

import com.orhanobut.logger.Logger;
import com.sound.bridge.framework.message.SendMessageAdaptor;
import com.sound.bridge.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * client socket的基本方法
 * 1. 连接
 * 2. 发送
 * 3. 接收
 * 4. 解析接收的报文
 * 5. 重连
 * 6. 加密
 * 7. 断开
 */
public abstract class AbstractClientSocket<T> implements SendMessageAdaptor<T> {

    private static int requestId = 0;

    public String TAG;
    private final Lock lock;  // 同步锁
    private InetAddress inetAddress; // 服务器地址
    private int port;   // 服务器端口
    private int connectTimeout;
    public String threadName;
    private Socket socket;
    private boolean closed;
    private byte[] cache; // 未解析的数据缓存

    protected AbstractClientSocket(ClientBuilder builder) throws IOException {
        Objects.requireNonNull(builder);
        this.TAG = Objects.requireNonNull(builder.tag);
        this.port = Objects.requireNonNull(builder.port);
        this.inetAddress = Objects.requireNonNull(builder.inetAddress);
        this.threadName = StringUtils.isEmpty(builder.threadName) ? builder.tag : builder.threadName;
        this.socket = builder.socket;
        this.connectTimeout = builder.connectTimeout;
        if (socket == null) connection();
        this.lock = new ReentrantLock();
        this.closed = false;
    }

    /**
     * 获取远程IP
     * @return
     */
    public String getRemoteIp() {
        return socket.isConnected() ? socket.getInetAddress().getHostAddress() : null;
    }

    /**
     * 设置远程IP
     */
    public void setRemoteIp(String newIp) throws UnknownHostException {
        String currentIp = getRemoteIp();
        if (!newIp.equals(currentIp)) {
            inetAddress = ipToInetAddress(newIp);
        }
    }

    /**
     * 连接socket服务器
     * @throws IOException 
     */
    public void connection() throws IOException {
        if (socket != null) socket.close();
        socket = new Socket();
        socket.connect(new InetSocketAddress(inetAddress, port), connectTimeout);
    }

    /**
     * 当socket == null时返回一个新的socket对象。如果socket != null，则进行重连
     * @throws IOException 
     */
    public void reconnection() throws IOException {
        if (socket == null) {
            connection();
            return;
        }
        socket.close();
        socket.connect(new InetSocketAddress(inetAddress, port), connectTimeout);
    }

    /**
     * 返回当前socket是否已经连接过
     */
    public boolean isConnection() {
        return socket != null && socket.isConnected();
    }

    /**
     * 关闭连接
     */
    public boolean disconnection() {
        try {
            if (socket != null) socket.close();
            socket = null;
            Logger.e("Disconnection socket success, thread {}  ", TAG);
            return true;
        } catch (Exception e) {
            Logger.e(e, "Disconnection socket failed, hostname = " + inetAddress.getHostName());
            return false;
        }
    }

    /**
     * 向服务器发送字符串
     * @param message 不能为空
     */
    @Override
    public boolean send(T message) {
        if (message == null) throw new NullPointerException("message cannot be empty!");
        lock.lock();
        try {
            OutputStream os = socket.getOutputStream();
            message = setSender(message);
            os.write(encrypt(message));
            os.flush();
            afterSend(message);
            return true;
        } catch (Exception e) {
            Logger.e(e, "Send message error");
            sendError(message, e);
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 设置发送人, 发送时间等信息
     */
    public abstract T setSender(T message);

    /**
     * 获取报文中的requestId
     */
    public abstract String getRequestId(T message);

    /** 
     * 设置receive超时时间
     */
    public void setTimeout(int timeout) {
        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            Logger.e(e, e.getMessage());
        }
    }

    /**
     * 阻塞线程直到收取到服务器发送的数据
     * 需要循环监听，避免丢失数据
     * @return 单条报文
     */
    public T receive() {
        T message = null;
        int errorNum = 0;
        while (!closed()) {
            byte[] buffer = null;
            try {
                if (cache != null) {
                    int position = positionMessage(cache);
                    Logger.d("Cache position = " + position);
                    if (position > 0) {
                        message = parseMessage(cache, position);
                        int cLen = cache.length;
                        if (position == cLen) {
                            Logger.d("PARSE MESSAGE COMPLETE");
                            cache = null;
                        } else {
                            cLen -= position;
                            byte[] temp = new byte[cLen];
                            System.arraycopy(cache, position, temp, 0, cLen);
                            cache = temp;
                            Logger.d("HAS MORE THAN ONE MESSAGE, CACHE LENGTH = " + cache.length);
                        }
                        if (message != null && filter(message)) return message;
                        else message = null;
                    } else if (position < 0) {
                        Logger.e("Pos < 0, drop current cache! cache = " + Arrays.toString(cache));
                        cache = null;
                    }
                }
                InputStream is = socket.getInputStream();
                int b = is.read();
                if (b == -1) {
                    Logger.e("Client socket input stream was shutdown");
                    close();
                    break;
                }
                int ans = 0;
                int len = is.available() + 1;
                if (cache == null) {
                    buffer = new byte[len];
                } else {
                    ans = cache.length;
                    len += ans;
                    buffer = new byte[len];
                    System.arraycopy(cache, 0, buffer, 0, ans);
                }
                buffer[ans++] = (byte) b;
                while(ans < len) {
                    ans += is.read(buffer, ans, len - ans);
                }
                Logger.d("TOTAL BUFFER = " + buffer.length);
                int pos = positionMessage(buffer);
                Logger.d("Position = " + pos);
                if (pos > 0) {
                    message = parseMessage(buffer, pos);
                    int bLen = buffer.length;
                    if (pos == bLen) {
                        Logger.d("PARSE MESSAGE COMPLETE");
                        cache = null;
                    } else {
                        int cLen = bLen - pos;
                        cache = new byte[cLen];
                        System.arraycopy(buffer, pos, cache, 0, cLen);
                        Logger.d("HAS MORE THAN ONE MESSAGE, CACHE LENGTH = " + cache.length);
                    }
                    if (message != null && filter(message)) break;
                    else {
                        message = null;
                        continue;
                    }
                } else if (pos < 0) {
                    Logger.e("Pos < 0, drop current buffer and cache! buffer length = " + buffer.length);
                    Logger.e("buffer = " + Arrays.toString(buffer));
                    buffer = null;
                    cache = null;
                    message = null;
                    continue;
                }
                cache = buffer;
                Logger.d("MESSAGE NOT COMPLETE, CACHE length = " + cache.length);
            } catch (Exception e) {
                Logger.e(e, "Receive message error");
                if ((++errorNum == 5) || e instanceof SocketException || e instanceof SocketTimeoutException) {
                    break;
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e1) {}
            }
        }
        return message;
    }

    /**
     * 获取下一个requestId
     */
    protected static synchronized int getNextRequestId() {
        if (String.valueOf(requestId).length() > 5) {
            requestId = 0;
        }
        return requestId;
    }

    /**
     * 过滤不需要的报文
     * @param message
     * @return
     */
    public boolean filter(T message) {
        return true;
    }

    /**
     * 发送报文前对报文进行加密
     * @param message
     * @return
     */
    protected abstract byte[] encrypt(T message);

    /**
     * 定位报文位置
     * @param buffer 报文数据, 可以认为数据开头一定是报文的起始
     * @return 如果buffer不足1个报文，返回 0, 否则返回第一个报文的长度, 如果返回小于0，则会丢弃当前数据和缓存
     */
    protected abstract int positionMessage(byte[] buffer);

    /**
     * 解析报文二进制数据
     * @param buffer
     * @param length 长度
     * @return
     */
    protected abstract T parseMessage(byte[] buffer, int length);

    public void logon() {
    }

    /** 登出 */
    public void logoff() {
    }

    public void close() {
        if (closed()) return;
        logoff();
        disconnection();
        this.closed = true;
    }

    public boolean closed() {
        return this.closed;
    }

    public static class ClientBuilder {
        private InetAddress inetAddress;
        private int port;
        private Socket socket;
        private String tag;
        private String threadName;
        private int connectTimeout;

        public ClientBuilder() {
            this.connectTimeout = 5000; // 默认连接超时时间
        }

        public ClientBuilder ip(String ip) throws UnknownHostException {
            // 不需要反向查找IP，连接更快
            this.inetAddress = ipToInetAddress(ip);
            return this;
        }
        public ClientBuilder port(int port) {
            this.port = port;
            return this;
        }
        public ClientBuilder socket(Socket socket) {
            this.socket = socket;
            this.inetAddress = socket.getInetAddress();
            return this;
        }
        public ClientBuilder tag(String tag) {
            this.tag = tag;
            return this;
        }
        public ClientBuilder threadName(String threadName) {
            this.threadName = threadName;
            return this;
        }
        /**
         * 连接超时时间(毫秒)
         */
        public ClientBuilder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }
        @SuppressWarnings("unchecked")
        public <T> T build(Class<? extends AbstractClientSocket<?>> clazz) throws Exception {
            Constructor<? extends AbstractClientSocket<?>> constructor = clazz.getDeclaredConstructor(ClientBuilder.class);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(this);
        }
    }

    static InetAddress ipToInetAddress(String ip) throws UnknownHostException {
        String[] ipArray = ip.split("\\.");
        if (ipArray.length != 4) throw new RuntimeException("IP is incorrect, ip = " + ip);
        byte[] buffer = new byte[4];
        for (int i = 0; i < 4; i++) {
            buffer[i] = (byte) Integer.parseInt(ipArray[i]);
        }
        // 不需要反向查找IP，连接更快
        return InetAddress.getByAddress(buffer);
    }
}