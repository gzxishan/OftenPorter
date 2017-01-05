package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.annotation.PortInObj;

/**
 * <br>
 * Created by https://github.com/CLovinr on 2016/9/10.
 */
public class Person extends User
{
    @PortInObj.Nece("name")
    public String name;
    @PortInObj.Nece("myAge")
    public String myAge;


    @Override
    public String toString()
    {
        return super.toString()+":"+"name="+name+",age="+myAge;
    }

}
