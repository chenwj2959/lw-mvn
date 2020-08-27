package com.cwj.mvn.core;

import java.io.File;
import java.util.Arrays;
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
            fileStr.append("<a href=\"/\">./</a>\n");
            fileStr.append("<a href=\"../\">../</a>\n");
        }
        File[] children = root.listFiles();
        if (children != null) {
            for (String fileName : getSortFileName(children)) {
                if (cannotAccess(fileName)) continue; // 不显示禁止访问的文件
                boolean isFile = isFile(fileName);
                String aurl = fileName;
                if (!isFile) aurl += "/";
                fileStr.append("<a href=\"").append(aurl);
                if (isFile) fileStr.append("\" download=\"").append(fileName);
                fileStr.append("\">").append(aurl).append("</a>\n");
            }
        }
        String html = String.format(Constant.HTML_FILES, path, fileStr.toString());
        returnHtml(protocol, html, HttpMsg.OK, client);
        return true;
    }
    
    private String[] getSortFileName(File[] children) {
        String[] fileNames = new String[children.length];
        for (int i = 0; i < children.length; i++) {
            fileNames[i] = children[i].getName();
        }
        Arrays.sort(fileNames);
        return fileNames;
    }
}
