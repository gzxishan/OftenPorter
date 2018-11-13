package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.pbridge.PName;

/**
 * @author Created by https://github.com/CLovinr on 2018-11-13.
 */
public class OftenContextInfo
{
    private PName pName;
    private String contextName;

    public OftenContextInfo(PName pName, String contextName)
    {
        this.pName = pName;
        this.contextName = contextName;
    }

    public String getContextName()
    {
        return contextName;
    }

    public void setContextName(String contextName)
    {
        this.contextName = contextName;
    }

    public PName getName()
    {
        return pName;
    }

    public void setName(PName pName)
    {
        this.pName = pName;
    }
}
