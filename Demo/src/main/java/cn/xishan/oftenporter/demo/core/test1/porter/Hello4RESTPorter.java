package cn.xishan.oftenporter.demo.core.test1.porter;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.WObject;

/**
 * <pre>
 * 1.设置PortIn.tiedType=true，使得接口类中，使得各个类型的接口函数都有效。
 * 	不过非REST函数会有先于REST函数。
 * </pre>
 *
 * @author https://github.com/CLovinr 2016年9月16日 下午4:57:02
 */
@PortIn(tiedType = TiedType.REST)
public class Hello4RESTPorter
{

    @PortIn(nece = {"name"}, unece = {"msg"}, method = PortMethod.POST,
            tiedType = TiedType.REST)
    public Object add(WObject wObject)
    {
        String name = (String) wObject.fn[0];
        Object msg = wObject.fu[0];

        return name + ":" + msg + ":" + wObject.restValue;
    }

    @PortIn(value = "add", nece = "content", method = PortMethod.POST)
    public Object add2(WObject wObject)
    {
        return "content:" + wObject.fn[0];
    }
}
