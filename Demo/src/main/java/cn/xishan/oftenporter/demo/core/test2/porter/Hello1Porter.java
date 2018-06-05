package cn.xishan.oftenporter.demo.core.test2.porter;

import java.io.IOException;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortOut;
import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.WObject;

@PortIn
class Hello1Porter
{

    /**
     * <pre>
     * 1.PortOut的默认值是OutType.Object
     * 2.当没有使用PortOut注解时，默认输出类型也是OutType.Object
     * </pre>
     * 
     * @param wObject
     * @return
     */
    @PortIn(nece = { "name" })
    @PortOut()
    public Object say(WObject wObject)
    {
	String name = (String) wObject.fn[0];

	return "Hello World:" + name;
    }

    @PortIn(nece = { "name" })
    @PortOut(OutType.NO_RESPONSE)
    public void say2(WObject wObject)
    {
	String name = (String) wObject.fn[0];
	try
	{
	    wObject.getResponse().write("self:" + name);
	    /*
	     * 对于OutType.NoResponse,需要手动关闭。
	     */
	    wObject.getResponse().close();
	}
	catch (IOException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
}
