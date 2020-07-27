package com.cwj.mvn.constant;

import java.nio.charset.Charset;

public class Constant {

    public static final Charset UTF8 = Charset.forName("utf8");
    public static final String LOG_NAME = "logName";
    
    public static final String THREAD_LW_MVN = "lw-mvn";
    
    // 404 response
    public static final String LAST_MODIFIED = "Wed, 10 Aug 2016 15:08:35 GMT";
    public static final String HTML_404 = "<!DOCTYPE html>\n" + 
            "<html>\n" + 
            "<head>\n" + 
            "    <title>404 Not Found</title>\n" + 
            "</head>\n" + 
            "\n" + 
            "<body bgcolor=\"white\">\n" + 
            "    <center>\n" + 
            "        <h1>404 Not Found</h1>\n" + 
            "    </center>\n" + 
            "    <hr>\n" + 
            "    <center>lw-mvn</center>\n" + 
            "</body>\n" + 
            "\n" + 
            "</html>";
}
