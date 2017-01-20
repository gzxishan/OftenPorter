package cn.xishan.oftenporter.porter.core.pbridge;

import cn.xishan.oftenporter.porter.core.base.AppValues;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class PRequest implements WRequest, Cloneable
{
    protected String requestPath;
    protected PortMethod method;
    protected Map<String, Object> params;
    protected Object originRequest, originResponse;

    public PRequest(PortMethod method, String requestPath)
    {
        this(method, requestPath, true);
    }

    protected PRequest(PortMethod method, String requestPath, boolean initMap)
    {
        this.method = method;
        this.requestPath = requestPath;
        if (initMap)
        {
            params = new HashMap<>();
        }
    }

    public PRequest(WRequest request, String requestPath)
    {
        this(request.getMethod(), requestPath, true);
        params.putAll(request.getParameterMap());
        initOrigin(request);
    }

    protected void initOrigin(WRequest request)
    {
        this.originRequest = request.getOriginalRequest();
        this.originResponse = request.getOriginalResponse();
    }

    public PRequest(String requestPath)
    {
        this(PortMethod.GET, requestPath);
    }


    public PRequest withNewPath(String newPath)
    {
        return withNewPath(newPath, this, false);
    }

    public static PRequest withNewPath(String newPath, WRequest wRequest, boolean willCloneParamsMap)
    {
        PRequest request = new PRequest(wRequest.getMethod(), newPath, willCloneParamsMap);
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
    public Object getOriginalResponse()
    {
        return originResponse;
    }

    @Override
    public Object getOriginalRequest()
    {
        return originRequest;
    }

    public synchronized PRequest addParamAll(AppValues appValues)
    {
        if(appValues!=null){
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
        params.putAll(paramMap);
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
