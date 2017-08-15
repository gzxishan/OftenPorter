package cn.xishan.oftenporter.bridge.http;

import java.util.Map;

/**
 * Created by https://github.com/CLovinr on 2017/8/10.
 */
public class RequestData {
    Map<String, String> headers;
    Map<String, Object> params;

    public RequestData(Map<String, Object> params) {
        this.params = params;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public RequestData putParam(String name,Object value){
        params.put(name,value);
        return this;
    }

    public RequestData putHeader(String name,String value){
        headers.put(name,value);
        return this;
    }
}
