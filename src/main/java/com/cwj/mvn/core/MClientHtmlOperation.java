package com.cwj.mvn.core;

import java.io.File;
import java.util.HashMap;

import com.cwj.mvn.constant.Constant;
import com.cwj.mvn.framework.http.HttpMsg;
import com.cwj.mvn.framework.http.bean.HttpHeader;
import com.cwj.mvn.framework.http.bean.HttpRequest;
import com.cwj.mvn.framework.http.bean.HttpResponse;
import com.cwj.mvn.framework.socket.AbstractClientSocket;
import com.cwj.mvn.utils.StringUtils;

public class MClientHtmlOperation extends MClientOperation {

    @Override
    public Boolean handle(byte[] message, HashMap<String, Object> paramMap, AbstractClientSocket<byte[]> client) {
        HttpRequest request = (HttpRequest) paramMap.get(HTTP_REQUEST);
        String route = request.getRoute();
        String protocol = request.getProtocol();
        String path = request.getPath();
        if (StringUtils.isBlank(route) || StringUtils.isBlank(path) || route.equals("/")) { // 重定向到主页
            HttpResponse resp = new HttpResponse(protocol, HttpMsg.MOVE_PERMANENTLY);
            HttpHeader headers = new HttpHeader();
            headers.put(HttpHeader.CONNECTION, HttpHeader.KEEP_ALIVE);
            headers.put(HttpHeader.LAST_MODIFIED, Constant.LAST_MODIFIED);
            headers.put(HttpHeader.CONTENT_TYPE, HttpHeader.TYPE_HTML);
            headers.put(HttpHeader.LOCATION, ".." + Constant.LOCAL_URL_SUFFIX + "/");
            resp.setHeaders(headers);
            resp.send(client);
            return true;
        } else if (route.equals("/favicon.ico")) {
            // TODO logo处理
            returnHtml(protocol, Constant.HTML_404, HttpMsg.NOT_FOUND, client);
            return true;
        }
        
        File root = new File(Constant.LOCAL_REPOSITORY + path);
        if (!root.exists()) {
            returnHtml(protocol, Constant.HTML_404, HttpMsg.NOT_FOUND, client);
            return true;
        }
        StringBuilder fileStr = new StringBuilder();
        if (path.length() > 1) { // 不在根路径，添加../返回上一级a链接
            fileStr.append("<a href=\"../\">../</a>\n");
        }
        File[] childes = root.listFiles();
        if (childes != null) {
            for (File child : childes) {
                String fileName = child.getName();
                boolean isFile = isFile(fileName);
                if (!isFile) fileName += "/";
                fileStr.append("<a href=\"")
                    .append(fileName);
                if (isFile) fileStr.append("\" download=\"").append(child.getName());
                fileStr.append("\" title=\"")
                    .append(fileName)
                    .append("\">")
                    .append(fileName)
                    .append("</a>\n");
            }
        }
        String html = String.format(Constant.HTML_FILES, path, fileStr.toString());
        returnHtml(protocol, html, HttpMsg.OK, client);
        return true;
    }
    
    private boolean isFile(String fileName) {
        return fileName.endsWith("sha1") || fileName.endsWith("pom") || fileName.endsWith("jar");
    }
}
