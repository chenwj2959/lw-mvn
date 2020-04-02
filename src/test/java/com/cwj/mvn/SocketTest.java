package com.cwj.mvn;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cwj.mvn.constant.Constant;
import com.cwj.mvn.framework.BaseRunnable;
import com.cwj.mvn.framework.Settings;
import com.cwj.mvn.framework.ThreadPool;
import com.cwj.mvn.framework.http.bean.HttpRequest;

public class SocketTest {
    private static final Logger log = LoggerFactory.getLogger(SocketTest.class);

    public static void main(String[] args) throws Exception {
        Settings.loadSettings();
        try (ServerSocket server = new ServerSocket(8081)) {
            int index = 0;
            while (true) {
                Socket client = server.accept();
                ThreadPool.putServer(new SocketClient(index++, client));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    
    static class SocketClient extends BaseRunnable {
        
        private Socket client;
        public SocketClient(int num, Socket client) {
            super("Client " + num);
            this.client = client;
        }
        @Override
        public void loop() {
            while (run) {
                try {
                    InputStream is = client.getInputStream();
                    int b = is.read();
                    if (b == -1) {
                        break;
                    }
                    int total = is.available() + 1;
                    byte[] buffer = new byte[total];
                    buffer[0] = (byte) b;
                    int count = 1;
                    while (count < total) {
                        count += is.read(buffer, count, total - count);
                    }
                    log.info("Buffer = {}", Arrays.toString(buffer));
                    log.info("Recieve buffer string = {}", new String(buffer, Constant.UTF8));
                    
                    HttpRequest httpRequest = new HttpRequest(buffer);
                    System.out.println("Method: " + httpRequest.getMethod());
                    System.out.println("Protocol: " + httpRequest.getProtocol());
                    System.out.println("Route: " + httpRequest.getRoute());
                    System.out.println("Headers: " + httpRequest.getHeaders());
                    System.out.println("Parameters: " + httpRequest.getParameters());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    close();
                }
            }
        }
    }
    
    // POM
//    GET /repository/maven-public/org/glassfish/jersey/bundles/repackaged/project/2.23.2/project-2.23.2.pom HTTP/1.1
//    Cache-control: no-cache
//    Cache-store: no-store
//    Pragma: no-cache
//    User-Agent: Apache-Maven/3.6.3 (Java 1.8.0_121; Windows 10 10.0)
//    Host: 192.168.1.82:8081
//    Connection: Keep-Alive
//    Accept-Encoding: gzip,deflate
    
    // jar
//    GET /repository/maven-public/javax/ws/rs/javax.ws.rs-api/2.0.1/javax.ws.rs-api-2.0.1.jar HTTP/1.1
//    Cache-control: no-cache
//    Cache-store: no-store
//    Pragma: no-cache
//    User-Agent: Apache-Maven/3.6.3 (Java 1.8.0_121; Windows 10 10.0)
//    Host: 192.168.1.82:8081
//    Connection: Keep-Alive
//    Accept-Encoding: gzip,deflate
}
