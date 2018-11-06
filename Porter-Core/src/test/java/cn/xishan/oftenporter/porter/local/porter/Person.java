package cn.xishan.oftenporter.porter.local.porter;


import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;

import java.util.Date;

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

    @Nece("hobby[coding]")
    private String hobby;

    @Unece("hobby2[coding2]")
    private String hobby2;

    @Nece("birthday[2018-9-10]")
    private Date birthday;

    @Override
    public String toString()
    {
        return super
                .toString() + ":" + "name=" + name + ",age=" + myAge + ",hobby=" + hobby + ",hobby2=" + hobby2 + "," +
                "birthdy=" + birthday;
    }

}
