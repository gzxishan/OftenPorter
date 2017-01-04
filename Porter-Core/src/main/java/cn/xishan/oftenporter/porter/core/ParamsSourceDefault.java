package cn.xishan.oftenporter.porter.core;


import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.WRequest;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by https://github.com/CLovinr on 2016/7/24.
 */
class ParamsSourceDefault implements ParamSource
{
    private UrlDecoder.Result result;
    private WRequest request;

    public ParamsSourceDefault(UrlDecoder.Result result, WRequest request)
    {
        this.result = result;
        this.request = request;
    }

    @Override
    public Object getParam(String name)
    {
        Object rs = result.getParam(name);
        if (WPTool.isEmpty(rs))
        {
            rs = request.getParameter(name);
        }
        return rs;
    }

    @Override
    public void putNewParams(Map<String, ?> newParams)
    {
        result.putNewParams(newParams);
    }

    @Override
    public Enumeration<String> paramNames()
    {
        Enumeration<String> enumeration = new Enumeration<String>()
        {
            Enumeration<String> e1 = result.paramNames();
            Iterator<String> e2 = request.getParameterMap().keySet().iterator();

            @Override
            public boolean hasMoreElements()
            {
                return e1.hasMoreElements() || e2.hasNext();
            }

            @Override
            public String nextElement()
            {
                return e1.hasMoreElements() ? e1.nextElement() : e2.next();
            }
        };
        return enumeration;
    }
}
