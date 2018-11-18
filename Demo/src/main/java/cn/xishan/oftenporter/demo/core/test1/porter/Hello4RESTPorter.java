package cn.xishan.oftenporter.demo.core.test1.porter;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

/**
 * <pre>
 * 1.设置PortIn.tiedType=true，使得接口类中，使得各个类型的接口函数都有效。
 * 	不过非REST函数会有先于REST函数。
 * </pre>
 *
 * @author https://github.com/CLovinr 2016年9月16日 下午4:57:02
 */
@PortIn(tiedType = TiedType.METHOD)
public class Hello4RESTPorter
{

    @PortIn(nece = {"name"}, unece = {"msg"}, method = PortMethod.POST,
            tiedType = TiedType.METHOD)
    public Object add(OftenObject oftenObject)
    {
        String name = (String) oftenObject._fn[0];
        Object msg = oftenObject._fu[0];

        return name + ":" + msg + ":" + oftenObject.funTied();
    }

    @PortIn(value = "add", nece = "content", method = PortMethod.POST)
    public Object add2(OftenObject oftenObject)
    {
        return "content:" + oftenObject._fn[0];
    }
}
