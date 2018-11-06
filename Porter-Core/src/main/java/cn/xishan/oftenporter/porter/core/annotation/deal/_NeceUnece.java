package cn.xishan.oftenporter.porter.core.annotation.deal;

/**
 * @author Created by https://github.com/CLovinr on 2018-11-06.
 */
public class _NeceUnece
{
    String varName;
    Object defaultValue;

    public _NeceUnece()
    {
    }

    public _NeceUnece(String varName)
    {
        this.varName = varName;
    }

    public _NeceUnece(String varName, Object defaultValue)
    {
        this.varName = varName;
        this.defaultValue = defaultValue;
    }

    public String getVarName()
    {
        return varName;
    }

    public Object getDefaultValue()
    {
        return defaultValue;
    }
}
