package com.cwj.mvn.utils;

public class StringUtils {

    public static boolean isEmpty(CharSequence charSeq) {
        return charSeq == null || charSeq.length() == 0;
    }
    
    public static boolean isBlank(String charSeq) {
        return charSeq == null || charSeq.trim().length() == 0;
    }
}
