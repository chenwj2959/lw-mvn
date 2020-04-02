package com.cwj.mvn.framework.http;

import java.io.File;
import java.util.HashMap;

import com.cwj.mvn.framework.Settings;
import com.cwj.mvn.framework.ThreadPool;
import com.cwj.mvn.framework.http.bean.HttpRequest;
import com.cwj.mvn.framework.http.bean.HttpResponse;
import com.cwj.mvn.framework.socket.AbstractClientSocket;
import com.cwj.mvn.framework.socket.AbstractOperation;

public class MClientOperation extends AbstractOperation<byte[]> {
    
    private static final String LOCAL_REPOSITORY = Settings.getSetting(Settings.LOCAL_REPOSITORY);
    private static final String REMOTE_URL = Settings.getSetting(Settings.REMOTE_URL);
    private static final String REMOTE_DOMAIN = REMOTE_URL.substring(0, REMOTE_URL.indexOf("\\/"));
    private static final String REMOTE_ROUTE = REMOTE_URL.substring(REMOTE_URL.indexOf("\\/") + 1);

    @Override
    public Boolean handle(byte[] message, HashMap<String, Object> paramMap, AbstractClientSocket<byte[]> client) {
        HttpRequest request = new HttpRequest(message);
        String route = request.getRoute();
        String filepath = LOCAL_REPOSITORY + route;
        File resource = new File(filepath);
        if (resource.exists()) { // 服务器已有，直接返回
            HttpResponse resp = new HttpResponse(request.getProtocol());
            resp.setRespFile(resource);
            resp.send(client);
        }
        // TODO 服务器没有，从远程仓库下载
        try {
            MClientSocket mClient = new MClientSocket.ClientBuilder()
                    .domain(REMOTE_DOMAIN)
                    .connectTimeout(5000)
                    .tag(route)
                    .build(MClientSocket.class);
            mClient.connection();
            mClient.send(message);
            byte[] buffer = mClient.receive();
            HttpResponse resp = HttpResponse.parse(buffer);
            if (resp.getHttpCode() == 200) {
                
            }
        } catch (Exception e) {
            
        }
        return true;
    }
}
