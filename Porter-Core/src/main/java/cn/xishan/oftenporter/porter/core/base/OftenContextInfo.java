package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.bridge.BridgeName;

/**
 * @author Created by https://github.com/CLovinr on 2018-11-13.
 */
public class OftenContextInfo
{
    private BridgeName bridgeName;
    private String contextName;

    public OftenContextInfo(BridgeName bridgeName, String contextName)
    {
        this.bridgeName = bridgeName;
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

    public BridgeName getName()
    {
        return bridgeName;
    }

    public void setName(BridgeName bridgeName)
    {
        this.bridgeName = bridgeName;
    }
}
