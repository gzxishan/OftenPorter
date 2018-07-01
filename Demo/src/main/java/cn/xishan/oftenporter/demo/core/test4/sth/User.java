package cn.xishan.oftenporter.demo.core.test4.sth;


import cn.xishan.oftenporter.porter.core.annotation.param.Nece;
import cn.xishan.oftenporter.porter.core.annotation.param.Unece;

public class User
{
    @Unece
    public int age;
    @Nece
    private String name;

    @Override
    public String toString()
    {
	return "name=" + name + ",age=" + age;
    }
}
