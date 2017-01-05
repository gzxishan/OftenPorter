package cn.xishan.oftenporter.demo.core.test4.sth;

import cn.xishan.oftenporter.porter.core.annotation.PortInObj.Nece;
import cn.xishan.oftenporter.porter.core.annotation.PortInObj.UnNece;

public class User
{
    @UnNece
    public int age;
    @Nece
    private String name;

    @Override
    public String toString()
    {
	return "name=" + name + ",age=" + age;
    }
}
