package com.cwj.mvn;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cwj.mvn.constant.Constant;
import com.cwj.mvn.framework.BaseRunnable;
import com.cwj.mvn.framework.ThreadPool;

public class SocketTest {
    private static final Logger log = LoggerFactory.getLogger(SocketTest.class);

    public static void main(String[] args) {
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
                        close();
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
                    
                    File jar = new File("C:\\Demo\\Java\\Environment\\jar\\com\\alibaba\\fastjson\\1.2.62\\fastjson-1.2.62.jar");
                    try (FileInputStream fis = new FileInputStream(jar)) {
                        FileChannel fc = fis.getChannel();
                        SocketChannel sc = client.getChannel();
                        long startTime = System.nanoTime();
                        long transferNum = fc.transferTo(0, fc.size(), sc);
                        log.info("Transfer {} byte done, spend {} ms", transferNum, System.nanoTime() - startTime);
                    } catch (Exception e) {
                        
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    close();
                }
            }
        }
    }
}
