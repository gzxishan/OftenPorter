package cn.xishan.oftenporter.demo.core.test4.porter;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.param.Parse;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
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
@Parse(paramNames = "age", parser = IntParser.class)
public class Hello1Porter
{
    /**
     * <pre>
     * 1.当函数上的与类上的Parser.parse出现同名时，则使用函数上的绑定。
     * </pre>
     * 
     * @param oftenObject
     * @return
     */

    @PortIn(method = PortMethod.POST, nece = { "age" })
    @Parse(paramNames = "age", parser = ShortParser.class)
    public Object say(OftenObject oftenObject)
    {
	short age = (short) oftenObject.fn[0];
	return age + "岁";
    }

    @PortIn(method = PortMethod.POST, nece = { "age" })
    public Object say2(OftenObject oftenObject)
    {
	int age = (int) oftenObject.fn[0];
	return age + "岁";
    }

    @PortIn(method = PortMethod.POST, nece = { "age" })
    public Object say3(OftenObject oftenObject)
    {
	short age = (short) oftenObject.fn[0];
	return age + "岁";
    }

}
