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
    }
}
