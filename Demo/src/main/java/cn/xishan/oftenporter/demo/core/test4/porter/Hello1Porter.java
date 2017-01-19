package cn.xishan.oftenporter.demo.core.test4.porter;

import cn.xishan.oftenporter.porter.core.annotation.Parser;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.simple.parsers.IntParser;
import cn.xishan.oftenporter.porter.simple.parsers.ShortParser;

/**
 * <pre>
 * 1.通过@Parser可以添加多个类型转换的绑定，也可以直接使用@Parser.parse
 * 2.更多内置的类型转换工具见包cn.xishan.oftenporter.porter.simple.parsers。
 * </pre>
 * 
 * @author https://github.com/CLovinr <br>
 *         2016年9月17日 下午2:57:58
 *
 */
@PortIn
@Parser({ @Parser.parse(paramNames = "age", parser = IntParser.class) })
public class Hello1Porter
{
    /**
     * <pre>
     * 1.当函数上的与类上的Parser.parse出现同名时，则使用函数上的绑定。
     * </pre>
     * 
     * @param wObject
     * @return
     */

    @PortIn(method = PortMethod.POST, nece = { "age" })
    @Parser({ @Parser.parse(paramNames = "age", parser = ShortParser.class) })
    public Object say(WObject wObject)
    {
	short age = (short) wObject.fn[0];
	return age + "岁";
    }

    @PortIn(method = PortMethod.POST, nece = { "age" })
    public Object say2(WObject wObject)
    {
	int age = (int) wObject.fn[0];
	return age + "岁";
    }

    @PortIn(method = PortMethod.POST, nece = { "age" })
    public Object say3(WObject wObject)
    {
	short age = (short) wObject.fn[0];
	return age + "岁";
    }

}
