package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.annotation.param.Nece;

/**
 * Created by https://github.com/CLovinr on 2016/9/8.
 */
public class User
{
    @Nece("name")
    public String name;
    @Nece(varName = "myAge")
    public int myAge;

    @Override
    public String toString()
    {
        return "name=" + name + ",age=" + myAge;
    }
}
