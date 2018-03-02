package cn.xishan.oftenporter.porter.local.porter2;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortInit;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.LogUtil;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/13.
 */
@PortIn("My2")
public class My2Porter
{
    @PortIn("hello")
    public String hello()
    {
        return getClass().getSimpleName();
    }

    @PortIn
    @PortInit(order = 99)
    public void init99()
    {
        LogUtil.printErrPos();
    }

    @PortIn
    @PortInit(order = 1)
    public void init1(WObject wObject)
    {
        wObject.setAttribute("name","tome");
        LogUtil.printErrPos();
    }

    @PortIn
    @PortInit(order = 2)
    public void init2(WObject wObject)
    {
        LogUtil.printErrPos();
    }
}
