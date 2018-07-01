package cn.xishan.oftenporter.demo.core.test4.sth;


import cn.xishan.oftenporter.porter.core.annotation.param.Nece;

public class ID
{
    @Nece
    String num;
    @Nece
    String addr;

    @Override
    public String toString()
    {
	return "ID:" + num + "," + addr;
    }
}
