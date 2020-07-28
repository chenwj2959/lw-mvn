package com.cwj.mvn.core;

import java.io.File;
import java.util.HashMap;

import com.cwj.mvn.constant.Constant;
import com.cwj.mvn.framework.http.HttpMsg;
import com.cwj.mvn.framework.http.bean.HttpRequest;
import com.cwj.mvn.framework.socket.AbstractClientSocket;

public class MClientHtmlOperation extends MClientOperation {

    @Override
    public Boolean handle(byte[] message, HashMap<String, Object> paramMap, AbstractClientSocket<byte[]> client) {
        HttpRequest request = (HttpRequest) paramMap.get(HTTP_REQUEST);
        String path = request.getPath();
        String protocol = request.getProtocol();
        File root = new File(LOCAL_REPOSITORY + path);
        if (!root.exists()) {
            returnHtml(protocol, Constant.HTML_404, HttpMsg.NOT_FOUND, client);
            return true;
        }
        StringBuilder fileStr = new StringBuilder();
        File[] childes = root.listFiles();
        if (childes != null) {
            for (File child : childes) {
                String fileName = child.getName();
                fileStr.append("<a href=\"")
                    .append(fileName)
                    .append("/\" title=\"")
                    .append(fileName)
                    .append("/\">")
                    .append(fileName)
                    .append("/</a>\n");
            }
        }
        String html = String.format(Constant.HTML_FILES, path, fileStr.toString());
        returnHtml(protocol, html, HttpMsg.OK, client);
        return true;
    }
}
