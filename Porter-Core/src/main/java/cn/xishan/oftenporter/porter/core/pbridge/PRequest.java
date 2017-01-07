package cn.xishan.oftenporter.porter.core.pbridge;

import cn.xishan.oftenporter.porter.core.base.AppValues;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WRequest;
import cn.xishan.oftenporter.porter.core.base.WResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class PRequest implements WRequest, Cloneable {
    protected String requestPath;
    protected PortMethod method;
    protected HashMap<String, Object> params;

    public PRequest(PortMethod method, String requestPath) {
        this(method, requestPath, true);
    }

    protected PRequest(PortMethod method, String requestPath, boolean initMap) {
        this.method = method;
        this.requestPath = requestPath;
        if (initMap) {
            params = new HashMap<>();
        }
    }

    public PRequest(WRequest request, String requestPath) {
        this(request.getMethod(), requestPath, true);
        params.putAll(request.getParameterMap());
    }

    public PRequest(String requestPath) {
        this(PortMethod.GET, requestPath);
    }


    public PRequest withNewPath(String newPath) {
        try {
            PRequest request = (PRequest) clone();
            request.requestPath = newPath;
            return request;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getParameter(String name) {
        return params.get(name);
    }

    @Override
    public Map<String, Object> getParameterMap() {
        return params;
    }


    @Override
    public String getPath() {
        return requestPath;
    }

    @Override
    public PortMethod getMethod() {
        return method;
    }

    @Override
    public Object getOriginalResponse() {
        return null;
    }

    @Override
    public Object getOriginalRequest() {
        return null;
    }

    public PRequest addParamAll(AppValues appValues) {
        String[] names = appValues.getNames();
        Object[] values = appValues.getValues();
        for (int i = 0; i < names.length; i++) {
            params.put(names[i], values[i]);
        }
        return this;
    }

    public PRequest addParamAll(Map<String, Object> paramMap) {
        params.putAll(paramMap);
        return this;
    }

    public PRequest addParam(String name, Object value) {
        params.put(name, value);
        return this;
    }

    public PRequest setRequestPath(String requestPath) {
        this.requestPath = requestPath;
        return this;
    }

    public PRequest setMethod(PortMethod method) {
        this.method = method;
        return this;
    }

}
