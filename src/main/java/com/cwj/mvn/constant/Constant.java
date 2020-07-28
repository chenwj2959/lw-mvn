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
    public static final String HTML_FILES = "<!DOCTYPE html>\n" + 
            "<html>\n" + 
            "<head>\n" + 
            "    <title>Central Repository: ant-contrib</title>\n" + 
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" + 
            "    <style>\n" + 
            "body {\n" + 
            "    background: #fff;\n" + 
            "}\n" + 
            "    </style>\n" + 
            "</head>\n" + 
            "\n" + 
            "<body>\n" + 
            "    <header>\n" + 
            "        <h1>%s</h1>\n" + 
            "    </header>\n" + 
            "    <hr/>\n" + 
            "    <main>\n" + 
            "        <pre id=\"contents\">\n" + 
            "<a href=\"../\">../</a>\n%s" + 
            "        </pre>\n" + 
            "    </main>\n" + 
            "    <hr/>\n" + 
            "</body>\n" + 
            "\n" + 
            "</html>";
}
