package cn.xishan.oftenporter.porter.simple;


import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.base.WRequest;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.util.*;

/**
 * 默认的参数源
 * Created by https://github.com/CLovinr on 2016/7/24.
 */
public class DefaultParamsSource implements ParamSource
{
    private UrlDecoder.Result result;
    private WRequest request;
    protected boolean hasRequestParameter = true;

    public DefaultParamsSource(UrlDecoder.Result result, WRequest request)
    {
        this.result = result;
        this.request = request;
    }

    @Override
    public <T> T getParam(String name)
    {
        Object rs = result.getParam(name);
        if (WPTool.isEmpty(rs))
        {
            rs = request.getParameter(name);
        }
        return (T) rs;
    }

    /**
     * 添加到{@linkplain cn.xishan.oftenporter.porter.core.base.UrlDecoder.Result}中去。
     *
     * @param newParams
     */
    @Override
    public void putNewParams(Map<String, ?> newParams)
    {
        result.putNewParams(newParams);
    }

    @Override
    public Enumeration<Map.Entry<String, Object>> params()
    {
        Enumeration<Map.Entry<String, Object>> e = new Enumeration<Map.Entry<String, Object>>()
        {
            Enumeration<String> names = paramNames();

            @Override
            public boolean hasMoreElements()
            {
                return names.hasMoreElements();
            }

            @Override
            public Map.Entry<String, Object> nextElement()
            {
                if (!names.hasMoreElements())
                {
                    throw new RuntimeException("no more element!");
                }
                String name = names.nextElement();
                Object value = result.getParam(name);
                if (WPTool.isEmpty(value))
                {
                    value = request.getParameter(name);
                }
                final Object finalValue = value;
                Map.Entry<String, Object> entry = new Map.Entry<String, Object>()
                {
                    Object value = finalValue;

                    @Override
                    public Object setValue(Object value)
                    {
                        Object v = this.value;
                        this.value = value;
                        return v;
                    }

                    @Override
                    public Object getValue()
                    {
                        return finalValue;
                    }

                    @Override
                    public String getKey()
                    {
                        return name;
                    }
                };
                return entry;
            }
        };
        return e;
    }

    @Override
    public Enumeration<String> paramNames()
    {
        Set<String> names = new HashSet<>();

        Enumeration<String> e1 = result.paramNames();
        while (e1.hasMoreElements())
        {
            names.add(e1.nextElement());
        }
        if (hasRequestParameter)
        {
            Iterator<String> e2 = request.getParameterMap().keySet().iterator();
            while (e2.hasNext())
            {
                names.add(e2.next());
            }
        }


        return new EnumerationImpl<>(names);
    }
}
