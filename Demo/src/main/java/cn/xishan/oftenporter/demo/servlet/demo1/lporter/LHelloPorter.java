package cn.xishan.oftenporter.demo.servlet.demo1.lporter;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;

@PortIn
public class LHelloPorter
{

    @PortIn
    public Object say()
    {
	return "Local Hello World!";
    }
}
