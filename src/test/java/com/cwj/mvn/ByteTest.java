package com.cwj.mvn;

public class ByteTest {
    
    private static byte[] DATA = {71, 69, 84, 32, 47, 32, 72, 84, 84, 80, 47, 49, 46, 49, 13, 10, 72, 111, 115, 116, 58, 32, 108, 111, 99, 97, 108, 104, 111, 115, 116, 58, 56, 48, 56, 49, 13, 10, 67, 111, 110, 110, 101, 99, 116, 105, 111, 110, 58, 32, 107, 101, 101, 112, 45, 97, 108, 105, 118, 101, 13, 10, 85, 112, 103, 114, 97, 100, 101, 45, 73, 110, 115, 101, 99, 117, 114, 101, 45, 82, 101, 113, 117, 101, 115, 116, 115, 58, 32, 49, 13, 10, 85, 115, 101, 114, 45, 65, 103, 101, 110, 116, 58, 32, 77, 111, 122, 105, 108, 108, 97, 47, 53, 46, 48, 32, 40, 87, 105, 110, 100, 111, 119, 115, 32, 78, 84, 32, 49, 48, 46, 48, 59, 32, 87, 105, 110, 54, 52, 59, 32, 120, 54, 52, 41, 32, 65, 112, 112, 108, 101, 87, 101, 98, 75, 105, 116, 47, 53, 51, 55, 46, 51, 54, 32, 40, 75, 72, 84, 77, 76, 44, 32, 108, 105, 107, 101, 32, 71, 101, 99, 107, 111, 41, 32, 67, 104, 114, 111, 109, 101, 47, 56, 48, 46, 48, 46, 51, 57, 56, 55, 46, 49, 52, 57, 32, 83, 97, 102, 97, 114, 105, 47, 53, 51, 55, 46, 51, 54, 13, 10, 83, 101, 99, 45, 70, 101, 116, 99, 104, 45, 68, 101, 115, 116, 58, 32, 100, 111, 99, 117, 109, 101, 110, 116, 13, 10, 65, 99, 99, 101, 112, 116, 58, 32, 116, 101, 120, 116, 47, 104, 116, 109, 108, 44, 97, 112, 112, 108, 105, 99, 97, 116, 105, 111, 110, 47, 120, 104, 116, 109, 108, 43, 120, 109, 108, 44, 97, 112, 112, 108, 105, 99, 97, 116, 105, 111, 110, 47, 120, 109, 108, 59, 113, 61, 48, 46, 57, 44, 105, 109, 97, 103, 101, 47, 119, 101, 98, 112, 44, 105, 109, 97, 103, 101, 47, 97, 112, 110, 103, 44, 42, 47, 42, 59, 113, 61, 48, 46, 56, 44, 97, 112, 112, 108, 105, 99, 97, 116, 105, 111, 110, 47, 115, 105, 103, 110, 101, 100, 45, 101, 120, 99, 104, 97, 110, 103, 101, 59, 118, 61, 98, 51, 59, 113, 61, 48, 46, 57, 13, 10, 83, 101, 99, 45, 70, 101, 116, 99, 104, 45, 83, 105, 116, 101, 58, 32, 110, 111, 110, 101, 13, 10, 83, 101, 99, 45, 70, 101, 116, 99, 104, 45, 77, 111, 100, 101, 58, 32, 110, 97, 118, 105, 103, 97, 116, 101, 13, 10, 83, 101, 99, 45, 70, 101, 116, 99, 104, 45, 85, 115, 101, 114, 58, 32, 63, 49, 13, 10, 65, 99, 99, 101, 112, 116, 45, 69, 110, 99, 111, 100, 105, 110, 103, 58, 32, 103, 122, 105, 112, 44, 32, 100, 101, 102, 108, 97, 116, 101, 44, 32, 98, 114, 13, 10, 65, 99, 99, 101, 112, 116, 45, 76, 97, 110, 103, 117, 97, 103, 101, 58, 32, 101, 110, 44, 122, 104, 45, 67, 78, 59, 113, 61, 48, 46, 57, 44, 122, 104, 59, 113, 61, 48, 46, 56, 13, 10, 13, 10};
    
    public static void main(String[] args) {
        byte[] target = {13, 10, 13, 10};
        System.out.println(new String(target));
        System.out.println(indexOf(DATA, 0, DATA.length, target, 0, target.length, 0));
    }
    /**
     * Code shared by String and StringBuffer to do searches. The
     * source is the character array being searched, and the target
     * is the string being searched for.
     *
     * @param   source       the characters being searched.
     * @param   sourceOffset offset of the source string.
     * @param   sourceCount  count of the source string.
     * @param   target       the characters being searched for.
     * @param   targetOffset offset of the target string.
     * @param   targetCount  count of the target string.
     * @param   fromIndex    the index to begin searching from.
     */
    static int indexOf(byte[] source, int sourceOffset, int sourceCount,
            byte[] target, int targetOffset, int targetCount,
            int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        byte first = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = i + targetCount;
                for (int k = targetOffset + 1; j < end && source[j]
                        == target[k]; j++, k++);

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }
}
