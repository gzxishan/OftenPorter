package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.base.UrlDecoder;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
class DefaultUrlResult implements UrlDecoder.Result
{
    private Map<String, Object> params;
    private String contextName, classTied, funTied;
    private Stack<UrlDecoder.TiedValue> tiedValueStack;

    public DefaultUrlResult(Map<String, Object> params, String contextName, String classTied, String funTied)
    {
        this.params = params;
        this.contextName = contextName;
        this.classTied = classTied;
        this.funTied = funTied;
    }

    @Override
    public String toString()
    {
        return "/" + contextName + "/" + classTied + "/" + funTied;
    }

    @Override
    public String contextName()
    {
        return contextName;
    }

    @Override
    public String classTied()
    {
        String tied = tiedValueStack == null || tiedValueStack.empty() ? classTied : tiedValueStack.peek().classTied;
        if (tied == null)
        {
            tied = classTied;
        }
        return tied;
    }

    @Override
    public String funTied()
    {
        String tied = tiedValueStack == null || tiedValueStack.empty() ? funTied : tiedValueStack.peek().funTied;
        if (tied == null)
        {
            tied = funTied;
        }
        return tied;
    }

    @Override
    public void push(UrlDecoder.TiedValue tiedValue)
    {
        if (tiedValueStack == null)
        {
            tiedValueStack = new Stack<>();
        }
        tiedValueStack.push(tiedValue);
    }

    @Override
    public UrlDecoder.TiedValue pop()
    {
        if (tiedValueStack != null && !tiedValueStack.isEmpty())
        {
            return tiedValueStack.pop();
        } else
        {
            return null;
        }
    }


    @Override
    public void setParam(String name, Object value)
    {
        params.put(name, value);
    }

    @Override
    public Object getParam(String name)
    {
        return params.get(name);
    }

    @Override
    public void setUrlResult(UrlDecoder.Result result) {

    }

    @Override
    public <T> T getNeceParam(String name, String errmsgOfEmpty)
    {
        return DefaultParamSource.getNeceParamUtil(this,name,errmsgOfEmpty);
    }

    @Override
    public <T> T getNeceParam(String name)
    {
        return DefaultParamSource.getNeceParamUtil(this,name);
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
        return new EnumerationImpl<>(iterator);
    }

    @Override
    public Enumeration<Map.Entry<String, Object>> params()
    {
        Enumeration<Map.Entry<String, Object>> e = new EnumerationImpl(params.entrySet());
        return e;
    }
}
