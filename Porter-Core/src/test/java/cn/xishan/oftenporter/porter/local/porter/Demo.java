package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.annotation.param.Nece;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/14.
 */
public class Demo
{
    @Nece("name")
    private String name;


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
