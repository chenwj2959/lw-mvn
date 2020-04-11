package com.cwj.mvn.utils;

import java.util.ArrayList;
import java.util.List;

public class ByteUtils {
    
    /**
     * Split byte[] by byte
     */
    public static List<byte[]> split(byte[] source, byte target) {
        return split(source, new byte[] {target});
    }
    
    /**
     * Split byte[] by another byte[]
     */
    public static List<byte[]> split(byte[] source, byte[] target) {
        int sourceCount = source.length;
        int targetCount = target.length;
        List<byte[]> res = new ArrayList<>();
        if (targetCount >= sourceCount) {
            res.add(source);
            return res;
        }
        
        int fromIndex = 0;
        int index = -1;
        while ((index = indexOf(source, target, fromIndex)) != -1) {
            int total = index - fromIndex;
            byte[] temp = new byte[total];
            System.arraycopy(source, fromIndex, temp, 0, total);
            res.add(temp);
            
            fromIndex = index + targetCount;
        }
        
        int remind = sourceCount - fromIndex;
        if (remind > 0) {
            byte[] temp = new byte[remind];
            System.arraycopy(source, fromIndex, temp, 0, remind);
            res.add(temp);
        }
        return res;
    }
    
    /**
     * Code shared by byte to do searches. The
     * source is the byte[] array being searched, and the target
     * is the byte being searched for.
     *
     * @param   source       the byte[] being searched.
     * @param   target       the byte being searched for.
     */
    public static int indexOf(byte[] source, byte target) {
        return indexOf(source, new byte[] {target}, 0);
    }
    
    /**
     * Code shared by byte[] to do searches. The
     * source is the byte[] array being searched, and the target
     * is the byte[] being searched for.
     *
     * @param   source       the byte[] being searched.
     * @param   target       the byte[] being searched for.
     */
    public static int indexOf(byte[] source, byte[] target) {
        return indexOf(source, target, 0);
    }
    
    /**
     * Code shared by byte[] to do searches. The
     * source is the byte[] array being searched, and the target
     * is the byte[] being searched for.
     *
     * @param   source       the byte[] being searched.
     * @param   target       the byte[] being searched for.
     * @param   fromIndex    the index to begin searching from.
     */
    public static int indexOf(byte[] source, byte[] target, int fromIndex) {
        int sourceCount = source.length;
        int targetCount = target.length;
        
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) return fromIndex;
        
        int targetOffset = 0;
        byte first = target[targetOffset];
        int max = sourceCount - targetCount;
        
        for (int i = fromIndex; i <= max; i++) {
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }
            
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = 1; j < end && source[j] == target[k]; j++, k++);

                if (j == end) {
                    return i;
                }
            }
        }
        return -1;
    }
}
