package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultNameValues;

import java.util.ArrayList;
import java.util.List;

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

    public static INameValues toNameValues(FunParam[] funParams)
    {
        DefaultNameValues defaultNameValues = new DefaultNameValues();
        for (FunParam funParam : funParams)
        {
            defaultNameValues.append(funParam.getName(), funParam.getValue());
        }
        return defaultNameValues;
    }

    /**
     * 如果元素非为{@linkplain FunParam},则name为objects[i].getClass().getName().
     *
     * @param objects
     * @return
     */
    public static INameValues toNameValues(Object... objects)
    {
        List<String> names = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Object obj : objects)
        {
            if (obj instanceof FunParam)
            {
                FunParam funParam = (FunParam) obj;
                if (OftenTool.isEmpty(funParam.getName()))
                {
                    throw new NullPointerException("empty FunParam name!");
                }
                names.add(funParam.getName());
                values.add(funParam.getValue());
            } else if (obj != null)
            {
                names.add(obj.getClass().getName());
                values.add(obj);
            }
        }
        DefaultNameValues defaultNameValues = new DefaultNameValues(names, values);
        return defaultNameValues;
    }
}
