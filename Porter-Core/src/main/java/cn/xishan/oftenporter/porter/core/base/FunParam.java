package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.simple.DefaultNameValues;

/**
 * @author Created by https://github.com/CLovinr on 2018-11-06.
 */
public class FunParam
{
    private String name;
    private Object value;

    public FunParam(String name, Object value)
    {
        this.name = name;
        this.value = value;
    }

    public FunParam()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public static INameValues toNameValues(FunParam[] funParams){
        DefaultNameValues defaultNameValues = new DefaultNameValues();
        for(FunParam funParam:funParams){
            defaultNameValues.append(funParam.getName(),funParam.getValue());
        }
        return defaultNameValues;
    }
}
