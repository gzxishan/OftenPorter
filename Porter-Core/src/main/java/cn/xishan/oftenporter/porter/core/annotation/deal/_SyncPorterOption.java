package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.base.PortMethod;

/**
 * Created by chenyg on 2017-04-26.
 */
public class _SyncPorterOption
{
    PortMethod method;
    String pathWithContext;

    public PortMethod getMethod()
    {
        return method;
    }

    public String getPathWithContext()
    {
        return pathWithContext;
    }
}
