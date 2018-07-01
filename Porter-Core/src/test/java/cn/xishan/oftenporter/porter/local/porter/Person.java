package cn.xishan.oftenporter.porter.local.porter;


import cn.xishan.oftenporter.porter.core.annotation.param.Nece;

/**
 * <br>
 * Created by https://github.com/CLovinr on 2016/9/10.
 */
public class Person extends User
{
    @Nece("name")
    public String name;
    @Nece("myAge")
    public String myAge;


    @Override
    public String toString()
    {
        return super.toString()+":"+"name="+name+",age="+myAge;
    }

}
