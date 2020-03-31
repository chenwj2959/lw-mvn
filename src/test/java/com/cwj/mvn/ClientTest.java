package com.cwj.mvn;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTest {
    
    private static final Logger log = LoggerFactory.getLogger(ClientTest.class);
    
    private static final byte[] pomMsg = {71, 69, 84, 32, 47, 114, 101, 112, 111, 115, 105, 116, 111, 114, 121, 47, 109, 97, 118, 101, 110, 45, 112, 117, 98, 108, 105, 99, 47, 111, 114, 103, 47, 103, 108, 97, 115, 115, 102, 105, 115, 104, 47, 106, 101, 114, 115, 101, 121, 47, 98, 117, 110, 100, 108, 101, 115, 47, 114, 101, 112, 97, 99, 107, 97, 103, 101, 100, 47, 112, 114, 111, 106, 101, 99, 116, 47, 50, 46, 50, 51, 46, 50, 47, 112, 114, 111, 106, 101, 99, 116, 45, 50, 46, 50, 51, 46, 50, 46, 112, 111, 109, 32, 72, 84, 84, 80, 47, 49, 46, 49, 13, 10, 67, 97, 99, 104, 101, 45, 99, 111, 110, 116, 114, 111, 108, 58, 32, 110, 111, 45, 99, 97, 99, 104, 101, 13, 10, 67, 97, 99, 104, 101, 45, 115, 116, 111, 114, 101, 58, 32, 110, 111, 45, 115, 116, 111, 114, 101, 13, 10, 80, 114, 97, 103, 109, 97, 58, 32, 110, 111, 45, 99, 97, 99, 104, 101, 13, 10, 85, 115, 101, 114, 45, 65, 103, 101, 110, 116, 58, 32, 65, 112, 97, 99, 104, 101, 45, 77, 97, 118, 101, 110, 47, 51, 46, 54, 46, 51, 32, 40, 74, 97, 118, 97, 32, 49, 46, 56, 46, 48, 95, 49, 50, 49, 59, 32, 87, 105, 110, 100, 111, 119, 115, 32, 49, 48, 32, 49, 48, 46, 48, 41, 13, 10, 72, 111, 115, 116, 58, 32, 49, 57, 50, 46, 49, 54, 56, 46, 49, 46, 56, 50, 58, 56, 48, 56, 49, 13, 10, 67, 111, 110, 110, 101, 99, 116, 105, 111, 110, 58, 32, 75, 101, 101, 112, 45, 65, 108, 105, 118, 101, 13, 10, 65, 99, 99, 101, 112, 116, 45, 69, 110, 99, 111, 100, 105, 110, 103, 58, 32, 103, 122, 105, 112, 44, 100, 101, 102, 108, 97, 116, 101, 13, 10, 13, 10};
    private static final byte[] jarMsg = {71, 69, 84, 32, 47, 114, 101, 112, 111, 115, 105, 116, 111, 114, 121, 47, 109, 97, 118, 101, 110, 45, 112, 117, 98, 108, 105, 99, 47, 106, 97, 118, 97, 120, 47, 119, 115, 47, 114, 115, 47, 106, 97, 118, 97, 120, 46, 119, 115, 46, 114, 115, 45, 97, 112, 105, 47, 50, 46, 48, 46, 49, 47, 106, 97, 118, 97, 120, 46, 119, 115, 46, 114, 115, 45, 97, 112, 105, 45, 50, 46, 48, 46, 49, 46, 106, 97, 114, 32, 72, 84, 84, 80, 47, 49, 46, 49, 13, 10, 67, 97, 99, 104, 101, 45, 99, 111, 110, 116, 114, 111, 108, 58, 32, 110, 111, 45, 99, 97, 99, 104, 101, 13, 10, 67, 97, 99, 104, 101, 45, 115, 116, 111, 114, 101, 58, 32, 110, 111, 45, 115, 116, 111, 114, 101, 13, 10, 80, 114, 97, 103, 109, 97, 58, 32, 110, 111, 45, 99, 97, 99, 104, 101, 13, 10, 85, 115, 101, 114, 45, 65, 103, 101, 110, 116, 58, 32, 65, 112, 97, 99, 104, 101, 45, 77, 97, 118, 101, 110, 47, 51, 46, 54, 46, 51, 32, 40, 74, 97, 118, 97, 32, 49, 46, 56, 46, 48, 95, 49, 50, 49, 59, 32, 87, 105, 110, 100, 111, 119, 115, 32, 49, 48, 32, 49, 48, 46, 48, 41, 13, 10, 72, 111, 115, 116, 58, 32, 49, 57, 50, 46, 49, 54, 56, 46, 49, 46, 56, 50, 58, 56, 48, 56, 49, 13, 10, 67, 111, 110, 110, 101, 99, 116, 105, 111, 110, 58, 32, 75, 101, 101, 112, 45, 65, 108, 105, 118, 101, 13, 10, 65, 99, 99, 101, 112, 116, 45, 69, 110, 99, 111, 100, 105, 110, 103, 58, 32, 103, 122, 105, 112, 44, 100, 101, 102, 108, 97, 116, 101, 13, 10, 13, 10};

    public static void main(String[] args) {
        try (Socket client = new Socket("192.168.1.12", 8081)) {
            OutputStream os = client.getOutputStream();
            os.write(pomMsg);
            os.flush();
            
            client.setSoTimeout(2000);
            byte[] cache = null;
            while (true) {
                try {
                    InputStream is = client.getInputStream();
                    int b = is.read();
                    if (b == -1) break;
                    int total = is.available() + 1;
                    byte[] buffer = new byte[total];
                    buffer[0] = (byte) b;
                    int offset = 1;
                    while (offset < total) {
                        offset += is.read(buffer, offset, total - offset);
                    }
                    if (cache == null) {
                        cache = buffer;
                    } else {
                        byte[] temp = new byte[cache.length + total];
                        System.arraycopy(cache, 0, temp, 0, cache.length);
                        System.arraycopy(buffer, 0, temp, cache.length, buffer.length);
                        cache = temp;
                    }
                } catch (Exception e) {
                    log.info("Buffer = " + Arrays.toString(cache));
                    log.info("Receive buffer string = " + new String(cache));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // jar response
//    HTTP/1.1 200 OK
//    Date: Tue, 31 Mar 2020 09:43:31 GMT
//    Server: Nexus/3.13.0-01 (OSS)
//    X-Content-Type-Options: nosniff
//    Content-Security-Policy: sandbox allow-forms allow-modals allow-popups allow-presentation allow-scripts allow-top-navigation
//    Last-Modified: Thu, 07 Aug 2014 12:08:10 GMT
//    ETag: "edcd111cf4d3ba8ac8e1f326efc37a17"
//    Content-Type: application/java-archive
//    Content-Length: 115534
    
    // POM response
//    HTTP/1.1 200 OK
//    Date: Tue, 31 Mar 2020 09:45:14 GMT
//    Server: Nexus/3.13.0-01 (OSS)
//    X-Content-Type-Options: nosniff
//    Content-Security-Policy: sandbox allow-forms allow-modals allow-popups allow-presentation allow-scripts allow-top-navigation
//    Last-Modified: Mon, 08 Aug 2016 17:44:33 GMT
//    ETag: "7aea9ac1dc2f8258187201386b98c7e7"
//    Content-Type: application/xml
//    Content-Length: 2792
}
