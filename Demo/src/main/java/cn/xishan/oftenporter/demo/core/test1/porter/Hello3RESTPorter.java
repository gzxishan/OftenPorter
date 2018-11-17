package cn.xishan.oftenporter.demo.core.test1.porter;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

/**
 * <pre>
 * 1.通过PortIn.tiedType设置接口类型。
 * 2.设置PortIn.tiedType=false，使得接口类中，只有REST类型的接口函数有效。
 * </pre>
 * 
 * @author https://github.com/CLovinr 2016年9月16日 下午4:57:02
 *
 */
@PortIn(tiedType = TiedType.REST)
public class Hello3RESTPorter
{
    /**
     * <pre>
     * 1.REST.
     * </pre>
     * 
     * @param oftenObject
     * @return
     */
    @PortIn(nece = { "name" }, unece = { "msg" }, method = PortMethod.POST,
	    tiedType = TiedType.REST)
    public Object add(OftenObject oftenObject)
    {
	String name = (String) oftenObject._fn[0];
	Object msg = oftenObject._fu[0];

	return name + ":" + msg + ":" + oftenObject.restValue;
    }


}
