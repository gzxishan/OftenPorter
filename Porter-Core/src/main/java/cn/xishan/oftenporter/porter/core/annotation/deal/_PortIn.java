package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.base.*;


/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class _PortIn
{
    String tiedName;
    InNames inNames;
    PortMethod method;
    Class<? extends CheckPassable>[] checks;
    TiedType tiedType;
    boolean isMultiTiedType;
    boolean ignoreTypeParser;

    public void setTiedType(TiedType tiedType)
    {
        this.tiedType = tiedType;
    }

    public boolean isMultiTiedType()
    {
        return isMultiTiedType;
    }

    public TiedType getTiedType()
    {
        return tiedType;
    }

    public Class<? extends CheckPassable>[] getChecks()
    {
        return checks;
    }

    public PortMethod getMethod()
    {
        return method;
    }

    public InNames getInNames()
    {
        return inNames;
    }

    public String getTiedName()
    {
        return tiedName;
    }

    public boolean ignoreTypeParser()
    {
        return ignoreTypeParser;
    }
}
