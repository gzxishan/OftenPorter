package cn.xishan.oftenporter.porter.core.pbridge;

import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.NotNull;
import cn.xishan.oftenporter.porter.core.base.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class PRequest implements WRequest
{
    protected String requestPath;
    protected PortMethod method;
    protected Map<String, Object> params;
    protected Object originRequest, originResponse;
    private WObject originalObject;

    public PRequest(@MayNull WObject originalObject, PortMethod method, String requestPath)
    {
        this(originalObject, method, requestPath, true);
    }


    public PRequest(PortMethod method, String requestPath)
    {
        this(null, method, requestPath, true);
    }

    /**
     * GET请求
     *
     * @param requestPath
     */
    public PRequest(String requestPath)
    {
        this(PortMethod.GET, requestPath);
    }

    protected PRequest(@MayNull WObject originalObject, PortMethod method, String requestPath, boolean initMap)
    {
        if (originalObject != null)
        {
            WObject wObject = originalObject.getRequest().getOriginalWObject();
            if (wObject != null)
            {
                originalObject = wObject;
            }
        }
        this.originalObject = originalObject;
        this.method = method;
        this.requestPath = requestPath;
        if (initMap)
        {
            params = new HashMap<>();
        }
    }

    public PRequest(@NotNull WObject wObject, String requestPath)
    {
        this(wObject, wObject.getRequest().getMethod(), requestPath, true);
        Enumeration<Map.Entry<String, Object>> e = originalObject.getParamSource().params();
        while (e.hasMoreElements())
        {
            Map.Entry<String, Object> entry = e.nextElement();
            params.put(entry.getKey(), entry.getValue());
        }
        initOrigin(originalObject.getRequest());
    }


    protected void initOrigin(WRequest request)
    {
        this.originRequest = request.getOriginalRequest();
        this.originResponse = request.getOriginalResponse();
    }


    @Override
    public WObject getOriginalWObject()
    {
        return originalObject;
    }

    public PRequest withNewPath(String newPath)
    {
        return withNewPath(originalObject, newPath, getMethod(), this, false);
    }

    public static PRequest withNewPath(WObject originalObject, String newPath, PortMethod method, WRequest wRequest,
            boolean willCloneParamsMap)
    {
        PRequest request = new PRequest(originalObject, method, newPath, willCloneParamsMap);
        request.originRequest = wRequest.getOriginalRequest();
        request.originResponse = wRequest.getOriginalResponse();
        if (willCloneParamsMap)
        {
            request.addParamAll(wRequest.getParameterMap());
        } else
        {
            request.params = wRequest.getParameterMap();
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

    public synchronized PRequest addParamAll(AppValues appValues)
    {
        if (appValues != null)
        {
            String[] names = appValues.getNames();
            Object[] values = appValues.getValues();
            for (int i = 0; i < names.length; i++)
            {
                params.put(names[i], values[i]);
            }
        }
        return this;
    }

    public synchronized PRequest addParamAll(Map<String, Object> paramMap)
    {
        if (paramMap != null)
        {
            params.putAll(paramMap);
        }
        return this;
    }

    public synchronized PRequest addParam(String name, Object value)
    {
        params.put(name, value);
        return this;
    }

    public PRequest setRequestPath(String requestPath)
    {
        this.requestPath = requestPath;
        return this;
    }

    public PRequest setMethod(PortMethod method)
    {
        this.method = method;
        return this;
    }

}
