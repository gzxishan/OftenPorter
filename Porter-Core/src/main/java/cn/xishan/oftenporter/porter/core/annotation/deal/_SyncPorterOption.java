package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.annotation.sth.PorterParamGetter;
import cn.xishan.oftenporter.porter.core.base.PortMethod;

/**
 * Created by chenyg on 2017-04-26.
 */
public class _SyncPorterOption
{
    PortMethod method;
    private String pathWithContext;
    PorterParamGetter porterParamGetter;

    public _SyncPorterOption(PorterParamGetter porterParamGetter)
    {
        this.porterParamGetter = porterParamGetter;
    }

    public PortMethod getMethod()
    {
        return method;
    }

    public String getPathWithContext()
    {
        return pathWithContext;
    }

    public void setOk()
    {
        pathWithContext = "/" + porterParamGetter.getContext() + "/" + porterParamGetter
                .getClassTied() + "/" + porterParamGetter.getFunTied();
        porterParamGetter = null;
    }
}
