package cn.xishan.oftenporter.demo.core.test1.porter;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

/**
 * <pre>
 * 1.通过{@linkplain PortIn#method()}设置请求方法
 * </pre>
 *
 * @author https://github.com/CLovinr 2016年9月11日 下午6:17:49
 *
 */
@PortIn
class Hello2Porter
{
    /**
     * <pre>
     * 1.设置成POST方法.
     * </pre>
     *
     * @param oftenObject
     * @return
     */
    @PortIn(nece = { "name" }, unece = { "msg" }, method = PortMethod.POST)
    public Object say(OftenObject oftenObject)
    {
	String name = (String) oftenObject._fn[0];
	Object msg = oftenObject._fu[0];

	return name + ":" + msg;
    }
}
