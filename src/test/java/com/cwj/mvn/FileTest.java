package com.cwj.mvn;

import java.io.File;
import java.io.FileOutputStream;

public class FileTest {

    public static void main(String[] args) {
        String filepath = "C://Demo/test.txt";
        File file = new File(filepath);
        System.out.println(file.exists());
        
        try (FileOutputStream fos = new FileOutputStream(filepath)) {
            fos.write("TEST".getBytes());
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println(file.exists());
        byte[] buffer = {71, 69, 84, 32, 47, 32, 72, 84, 84, 80, 47, 49, 46, 49, 13, 10, 72, 111, 115, 116, 58, 32, 52, 55, 46, 49, 49, 52, 46, 49, 56, 51, 46, 50, 57, 58, 56, 56, 56, 56, 13, 10, 85, 115, 101, 114, 45, 65, 103, 101, 110, 116, 58, 32, 77, 111, 122, 105, 108, 108, 97, 47, 53, 46, 48, 32, 40, 77, 97, 99, 105, 110, 116, 111, 115, 104, 59, 32, 73, 110, 116, 101, 108, 32, 77, 97, 99, 32, 79, 83, 32, 88, 32, 49, 48, 46, 49, 49, 59, 32, 114, 118, 58, 52, 55, 46, 48, 41, 32, 71, 101, 99, 107, 111, 47, 50, 48, 49, 48, 48, 49, 48, 49, 32, 70, 105, 114, 101, 102, 111, 120, 47, 52, 55, 46, 48, 13, 10, 65, 99, 99, 101, 112, 116, 58, 32, 42, 47, 42, 13, 10, 67, 111, 110, 110, 101, 99, 116, 105, 111, 110, 58, 32, 107, 101, 101, 112, 45, 97, 108, 105, 118, 101, 13, 10, 13, 10};
        System.out.println(new String(buffer));
    }
}
