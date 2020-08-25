package com.cwj.mvn.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.cwj.mvn.framework.http.bean.HttpHeader;
import com.cwj.mvn.framework.http.bean.HttpParameter;

public class HttpUtils {

    public static HttpURLConnection getConn(String urlStr, String method, Map<String, String> headers, Map<String, Object> parameters) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setUseCaches(true);
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(5000);
        for (Map.Entry<String, String> header : headers.entrySet()) {
            conn.setRequestProperty(header.getKey(), header.getValue());
        }
        if (StringUtils.isBlank(conn.getRequestProperty(HttpHeader.USER_AGENT))) {
            conn.setRequestProperty(HttpHeader.USER_AGENT, HttpHeader.DEFAULT_USER_AGENT);
        }
        conn.setDoInput(true);
        byte[] data = (byte[]) parameters.get(HttpParameter.DATA);
        if (data != null && data.length > 0) {
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write(data);
            os.flush();
            os.close();
        } else conn.setDoOutput(false);
        return conn;
    }
}
