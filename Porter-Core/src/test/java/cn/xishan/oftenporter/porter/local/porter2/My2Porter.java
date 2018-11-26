package cn.xishan.oftenporter.porter.local.porter2;

import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortInit;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
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
        return PortUtil.getRealClass(getClass()).getSimpleName();
    }

    @PortIn
    @PortInit(order = 99)
    public void init99()
    {
        LogUtil.printErrPos();
    }

    @PortIn
    @PortInit(order = 1)
    public void init1(OftenObject oftenObject) throws Exception
    {
        oftenObject.putRequestData("name", "tome");
        LogUtil.printErrPos();
    }

    @PortIn
    @PortInit(order = 2)
    public void init2(OftenObject oftenObject)
    {
        LogUtil.printErrPos();
    }
}
