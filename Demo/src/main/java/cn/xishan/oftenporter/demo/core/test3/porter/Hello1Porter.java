package cn.xishan.oftenporter.demo.core.test3.porter;

import cn.xishan.oftenporter.porter.core.annotation.PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortStart;

@PortIn
class Hello1Porter
{
    @PortStart
    public void onStart()
    {
	System.out.println("***************onStart>>>>>>"+getClass().getName());
    }

    @PortDestroy
    public void onDestroy()
    {
	System.out.println("***************onDestroy>>>>>"+getClass().getName());
    }
}
