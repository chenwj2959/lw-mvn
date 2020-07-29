package com.cwj.mvn.framework.http.bean;

import java.util.HashMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class HttpParameter extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;
    
    // Custom data key
    public static final String DATA = "Data";
    
    public void putParam(String contentType, byte[] data) {
        if (HttpHeader.TYPE_MULTIPART.equals(contentType)) {
            // FIXME 处理Multipart
            put(DATA, data);
        } else if (HttpHeader.TYPE_URLENCODE.equals(contentType)) {
            // XXX 确认urlencode处理方式
            putAll(JSON.parseObject("{" + new String(data) + "}"));
        } else if (HttpHeader.TYPE_TEXT_PLAIN.equals(contentType)) {
            put(DATA, new String(data));
        } else if (HttpHeader.TYPE_JSON.equals(contentType)) {
            putAll(JSON.parseObject(new String(data)));
        } else {
            // XXX 完善content-type
            put(DATA, data);
        }
    }
    
    public byte[] getData(String contentType) {
        if (HttpHeader.TYPE_MULTIPART.equals(contentType)) {
            // FIXME 处理Multipart
            return (byte[]) get(DATA);
        } else if (HttpHeader.TYPE_URLENCODE.equals(contentType)) {
            // XXX 确认urlencode处理方式
            return JSONObject.toJSONBytes(this);
        } else if (HttpHeader.TYPE_TEXT_PLAIN.equals(contentType)) {
            return String.valueOf(get(DATA)).getBytes();
        } else if (HttpHeader.TYPE_JSON.equals(contentType)) {
            return JSONObject.toJSONBytes(this);
        } else if (HttpHeader.TYPE_HTML.equals(contentType)) {
            return get(DATA).toString().getBytes();
        } else if (HttpHeader.TYPE_ICON.equals(contentType)) {
            // TODO 图片传输
            return (byte[]) get(DATA);
        } else {
            // XXX 完善content-type
            return (byte[]) get(DATA);
        }
    }
}
