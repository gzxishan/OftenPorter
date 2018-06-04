package cn.xishan.oftenporter.demo.core.test1.porter;

import cn.xishan.oftenporter.demo.core.test1.check.ClassCheckPassable;
import cn.xishan.oftenporter.demo.core.test1.check.ClassCheckPassable2;
import cn.xishan.oftenporter.demo.core.test1.check.MethodCheckPassable;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;

/**
 * <pre>
 * 1.PortIn.checks会根据顺序依次被调用
 * </pre>
 * 
 * @author https://github.com/CLovinr 2016年9月16日 下午5:33:24
 *
 */
@PortIn(checks = { ClassCheckPassable.class, ClassCheckPassable2.class })
public class Hello5Porter
{
    @PortIn(checks = { MethodCheckPassable.class })
    public Object say()
    {
	return "Hello";
    }
}
