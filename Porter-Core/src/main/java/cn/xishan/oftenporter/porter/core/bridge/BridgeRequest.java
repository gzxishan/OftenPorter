package cn.xishan.oftenporter.porter.core.bridge;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class BridgeRequest implements OftenRequest
{
    protected String requestPath;
    protected PortMethod method;
    protected Map<String, Object> params;
    protected Object originRequest, originResponse;
    private OftenObject originalObject;

    public BridgeRequest(@MayNull OftenObject originalObject, PortMethod method, String requestPath)
    {
        this(originalObject, method, requestPath, true);
    }


    public BridgeRequest(PortMethod method, String requestPath)
    {
        this(null, method, requestPath, true);
    }

    /**
     * GET请求
     *
     * @param requestPath
     */
    public BridgeRequest(String requestPath)
    {
        this(PortMethod.GET, requestPath);
    }

    protected BridgeRequest(@MayNull OftenObject originalObject, PortMethod method, String requestPath, boolean initMap)
    {
        if (originalObject != null)
        {
            OftenObject oftenObject = originalObject.getRequest().getOriginalObject();
            if (oftenObject != null)
            {
                originalObject = oftenObject;
            }
        }
        if (originalObject != null)
        {
            this.originalObject = originalObject;
            initOrigin(originalObject.getRequest());
        }
        this.method = method;
        this.requestPath = requestPath;
        if (initMap)
        {
            params = new HashMap<>();
        }
    }

    public BridgeRequest(@NotNull OftenObject oftenObject, String requestPath)
    {
        this(oftenObject, oftenObject.getRequest().getMethod(), requestPath, true);
        Enumeration<Map.Entry<String, Object>> e = originalObject.getParamSource().params();
        while (e.hasMoreElements())
        {
            Map.Entry<String, Object> entry = e.nextElement();
            params.put(entry.getKey(), entry.getValue());
        }
    }


    protected void initOrigin(OftenRequest request)
    {
        this.originRequest = request.getOriginalRequest();
        this.originResponse = request.getOriginalResponse();
    }


    @Override
    public OftenObject getOriginalObject()
    {
        return originalObject;
    }

    public BridgeRequest withNewPath(String newPath)
    {
        return withNewPath(originalObject, newPath, getMethod(), this, false);
    }

    public static BridgeRequest withNewPath(OftenObject originalObject, String newPath, PortMethod method,
            OftenRequest oftenRequest, boolean willCloneParamsMap)
    {
        BridgeRequest request = new BridgeRequest(originalObject, method, newPath, willCloneParamsMap);

        if (willCloneParamsMap)
        {
            request.addParamAll(oftenRequest.getParameterMap());
        } else
        {
            request.params = oftenRequest.getParameterMap();
        }
        return request;
    }

    @Override
    public synchronized Object getParameter(String name)
    {
        return params.get(name);
    }

    @Override
    public synchronized Map<String, Object> getParameterMap()
    {
        return params;
    }


    @Override
    public String getPath()
    {
        return requestPath;
    }

    @Override
    public PortMethod getMethod()
    {
        return method;
    }

    @Override
    public <T> T getOriginalResponse()
    {
        return (T) originResponse;
    }

    @Override
    public <T> T getOriginalRequest()
    {
        return (T) originRequest;
    }

    public synchronized BridgeRequest addParamAll(INameValues INameValues)
    {
        if (INameValues != null)
        {
            String[] names = INameValues.getNames();
            Object[] values = INameValues.getValues();
            for (int i = 0; i < names.length; i++)
            {
                params.put(names[i], values[i]);
            }
        }
        return this;
    }

    public synchronized BridgeRequest addParamAll(Map<String, Object> paramMap)
    {
        if (paramMap != null)
        {
            params.putAll(paramMap);
        }
        return this;
    }

    public synchronized BridgeRequest addParam(String name, Object value)
    {
        params.put(name, value);
        return this;
    }

    public BridgeRequest setRequestPath(String requestPath)
    {
        this.requestPath = requestPath;
        return this;
    }

    public BridgeRequest setMethod(PortMethod method)
    {
        this.method = method;
        return this;
    }

}
