package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.base.*;


/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class _PortIn
{
    String[] tiedNames, ignoredFunTieds;
    InNames inNames;
    PortMethod[] methods;
    Class<? extends CheckPassable>[] checks, checksForWholeClass;
    private TiedType tiedType;
    boolean ignoreTypeParser;

    PortFunType portFunType;
    AspectPosition aspectPosition;

    public _PortIn(PortFunType portFunType,AspectPosition aspectPosition, String[] ignoredFunTieds)
    {
        this.portFunType = portFunType;
        this.aspectPosition=aspectPosition;
        this.ignoredFunTieds = ignoredFunTieds;
    }

    public String[] getIgnoredFunTieds()
    {
        return ignoredFunTieds;
    }

    public PortFunType getPortFunType()
    {
        return portFunType;
    }

    public AspectPosition getAspectPosition()
    {
        return aspectPosition;
    }

    public TiedType getTiedType()
    {
        return tiedType;
    }

    public Class<? extends CheckPassable>[] getChecks()
    {
        return checks;
    }

    public Class<? extends CheckPassable>[] getCheckPassablesForWholeClass()
    {
        return checksForWholeClass;
    }


    public PortMethod[] getMethods()
    {
        return methods;
    }

    public InNames getInNames()
    {
        return inNames;
    }

    public String[] getTiedNames()
    {
        return tiedNames;
    }

    void setTiedNames(String[] tiedNames)
    {
        for (String tiedName : tiedNames)
        {
            PortUtil.checkName(tiedName);
        }
        this.tiedNames = tiedNames;
    }

    public boolean ignoreTypeParser()
    {
        return ignoreTypeParser;
    }

    void setTiedType(TiedType tiedType)
    {
        this.tiedType = tiedType;
    }
}
