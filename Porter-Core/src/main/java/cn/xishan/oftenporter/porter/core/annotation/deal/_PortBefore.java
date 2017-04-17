package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.base.PortMethod;

/**
 * Created by chenyg on 2017-04-17.
 */
public class _PortBefore
{
    String pathWithContext;

    PortMethod method;

    public _PortBefore(PortMethod method)
    {
        this.method = method;
    }

    public String getPathWithContext()
    {
        return pathWithContext;
    }

    public PortMethod getMethod()
    {
        return method;
    }
}
