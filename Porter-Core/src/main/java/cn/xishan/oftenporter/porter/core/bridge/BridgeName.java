package cn.xishan.oftenporter.porter.core.bridge;

import cn.xishan.oftenporter.porter.core.advanced.PortUtil;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/18.
 */
public class BridgeName
{
    private final String name;

    public BridgeName(String name)
    {
        if (name == null)
        {
            throw new NullPointerException();
        }
        PortUtil.checkName(name);

        this.name = name;
    }

    public final String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof BridgeName))
        {
            return false;
        }
        BridgeName bridgeName = (BridgeName) obj;
        return this.name.equals(bridgeName.name);
    }

    @Override
    public String toString()
    {
        return name;
    }
}
