package com.cwj.mvn.utils;

public class StringUtils {

    public static boolean isEmpty(CharSequence charSeq) {
        return charSeq == null || charSeq.length() == 0;
    }
}
