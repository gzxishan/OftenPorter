package cn.xishan.oftenporter.demo.servlet.demo1.porter;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortStart;
import cn.xishan.oftenporter.porter.core.annotation.param.Parse;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.simple.parsers.IntParser;
import cn.xishan.oftenporter.porter.simple.parsers.StringParser;

/**
 * Created by https://github.com/CLovinr on 2016/9/4.
 */
@PortIn(value = "", tiedType = TiedType.REST)
@Parse(paramNames = "age", parser = IntParser.class)
@Parse(paramNames = "sex", parser = StringParser.class)
public class HelloPorter
{

    // @AutoSet
    // private PersonAP personAP;

    @AutoSet
    private ArrayList<String> list;

    @PortIn(value = "say", nece = { "name", "age", "sex" })
    public Object say(WObject wObject)
    {
	int age = (int) wObject.fn[1];
	return "Hello World!" + wObject.fn[0]
		+ ",age="
		+ age
		+ ",sex="
		+ wObject.fn[2];
    }

    @PortIn(tiedType = TiedType.REST)
    public Object sayHello(WObject wObject)
    {
	HttpServletRequest request = (HttpServletRequest) wObject.getRequest().getOriginalRequest();
	return "Hello World-REST!" + wObject.restValue
		+ ":dt="
		+ ((System.nanoTime() - (long) request.getAttribute("time"))
			/ 1000000.0)+"ms";
    }


    @PortStart
    public void onStart()
    {
	list.add("tom");

    }

    @PortDestroy
    public void onDestroy()
    {
	LogUtil.printPosLn();
    }

}
