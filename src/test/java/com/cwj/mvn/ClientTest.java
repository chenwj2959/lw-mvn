package com.cwj.mvn;

import java.net.Socket;

public class ClientTest {
    
    private static final byte[] message = {71, 69, 84, 32, 47, 111, 114, 103, 47, 97, 112, 97, 99, 104, 101, 47, 109, 97, 118, 101, 110, 47, 112, 108, 117, 103, 105, 110, 115, 47, 109, 97, 118, 101, 110, 45, 99, 111, 109, 112, 105, 108, 101, 114, 45, 112, 108, 117, 103, 105, 110, 47, 51, 46, 49, 47, 109, 97, 118, 101, 110, 45, 99, 111, 109, 112, 105, 108, 101, 114, 45, 112, 108, 117, 103, 105, 110, 45, 51, 46, 49, 46, 112, 111, 109, 32, 72, 84, 84, 80, 47, 49, 46, 49, 13, 10, 67, 97, 99, 104, 101, 45, 99, 111, 110, 116, 114, 111, 108, 58, 32, 110, 111, 45, 99, 97, 99, 104, 101, 13, 10, 67, 97, 99, 104, 101, 45, 115, 116, 111, 114, 101, 58, 32, 110, 111, 45, 115, 116, 111, 114, 101, 13, 10, 80, 114, 97, 103, 109, 97, 58, 32, 110, 111, 45, 99, 97, 99, 104, 101, 13, 10, 85, 115, 101, 114, 45, 65, 103, 101, 110, 116, 58, 32, 65, 112, 97, 99, 104, 101, 45, 77, 97, 118, 101, 110, 47, 51, 46, 54, 46, 51, 32, 40, 74, 97, 118, 97, 32, 49, 46, 56, 46, 48, 95, 50, 51, 49, 59, 32, 87, 105, 110, 100, 111, 119, 115, 32, 49, 48, 32, 49, 48, 46, 48, 41, 13, 10, 72, 111, 115, 116, 58, 32, 49, 50, 55, 46, 48, 46, 48, 46, 49, 58, 56, 48, 56, 49, 13, 10, 67, 111, 110, 110, 101, 99, 116, 105, 111, 110, 58, 32, 75, 101, 101, 112, 45, 65, 108, 105, 118, 101, 13, 10, 65, 99, 99, 101, 112, 116, 45, 69, 110, 99, 111, 100, 105, 110, 103, 58, 32, 103, 122, 105, 112, 44, 100, 101, 102, 108, 97, 116, 101, 13, 10, 13, 10};

    public static void main(String[] args) {
        try (Socket client = new Socket()) {
            
        } catch (Exception e) {
            
        }
    }
}
