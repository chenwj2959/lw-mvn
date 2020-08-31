package com.cwj.mvn.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    // Tue, 31 Mar 2020 09:43:31 GMT
    public static final String EdMyHms_GMT = "EEE, d MMM yyyy HH:mm:ss 'GMT'";
    public static final String yMdHmsS = "yyyy-MM-dd HH:mm:ss.SSS";
    
    public static String dateToString(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        return sdf.format(date);
    }
    
    public static String dateToString(long date, String pattern) {
        return dateToString(new Date(date), pattern);
    }
    
    public static long stringToTimestamp(String dateStr, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
            return sdf.parse(pattern).getTime();
        } catch (ParseException e) {
            return new Date().getTime();
        }
    }
}
