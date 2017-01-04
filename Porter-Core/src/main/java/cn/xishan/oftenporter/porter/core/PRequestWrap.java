package cn.xishan.oftenporter.porter.core;

import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WRequest;
import cn.xishan.oftenporter.porter.core.pbridge.PRequest;

import java.util.Enumeration;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/28.
 */
class PRequestWrap extends PRequest
{
    private WRequest wRequest;

    public PRequestWrap(WRequest wRequest, String path)
    {
        super(wRequest.getMethod(), path,false);
        this.wRequest = wRequest;
    }

    @Override
    public Object getParameter(String name)
    {
        return wRequest.getParameter(name);
    }


    @Override
    public Map<String, Object> getParameterMap()
    {
        return wRequest.getParameterMap();
    }

    @Override
    public PortMethod getMethod()
    {
        return wRequest.getMethod();
    }
}
