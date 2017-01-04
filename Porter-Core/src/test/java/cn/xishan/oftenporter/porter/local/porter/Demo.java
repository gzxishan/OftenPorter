package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.annotation.PortInObj;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/14.
 */
public class Demo implements IDemo
{
    @PortInObj.Nece("name")
    private String name;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
