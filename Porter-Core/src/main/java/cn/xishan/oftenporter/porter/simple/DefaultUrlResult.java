package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
class DefaultUrlResult implements UrlDecoder.Result
{
    private Map<String, Object> params;
    private String contextName, classTied, funTied;

    public DefaultUrlResult(Map<String, Object> params, String contextName, String classTied, String funTied)
    {
        this.params = params;
        this.contextName = contextName;
        this.classTied = classTied;
        this.funTied = funTied;
    }


    @Override
    public String contextName()
    {
        return contextName;
    }

    @Override
    public String classTied()
    {
        return classTied;
    }

    @Override
    public String funTied()
    {
        return funTied;
    }

    @Override
    public void setParam(String name, Object value) {
        params.put(name,value);
    }

    @Override
    public Object getParam(String name)
    {
        return params.get(name);
    }

    @Override
    public void putNewParams(Map<String, ?> newParams)
    {
        params.putAll(newParams);
    }

    @Override
    public Enumeration<String> paramNames()
    {
        Iterator<String> iterator = params.keySet().iterator();
        return new EnumerationImpl<String>(iterator);
    }
}
