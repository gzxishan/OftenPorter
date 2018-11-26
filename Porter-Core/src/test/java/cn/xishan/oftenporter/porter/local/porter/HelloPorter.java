package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.annotation.param.BindEntities;
import cn.xishan.oftenporter.porter.core.annotation.param.MixinParseFrom;
import cn.xishan.oftenporter.porter.core.annotation.param.Parse;
import cn.xishan.oftenporter.porter.core.sysset.TypeTo;
import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.annotation.PortStart;
import cn.xishan.oftenporter.porter.core.annotation.PortDestroy;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.util.LogMethodInvoke;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.local.AspectHandle;
import cn.xishan.oftenporter.porter.local.mixin.HelloMixinPorter;
import cn.xishan.oftenporter.porter.local.mixin.MinxParseTest;
import cn.xishan.oftenporter.porter.local.proxy.ProxyUnit;
import cn.xishan.oftenporter.porter.simple.parsers.IntParser;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Random;

/**
 * Created by https://github.com/CLovinr on 2016/9/4.
 */
//@Parser({
//        @Parser.parse(paramNames = "age", parser = IntParser.class)
//})
@PortIn(tied = "Hello", tiedType = TiedType.METHOD)
@BindEntities({User.class})
@Mixin({HelloMixinPorter.class})
@MixinParseFrom({MinxParseTest.class})
public class HelloPorter extends SuperSetPorter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloPorter.class);


    @AutoSet
    private static AutoSetObj autoSetObj2;

    @AutoSet(range = AutoSet.Range.New)
    private AutoSetObj autoSetObj3;

    @Resource(shareable = false)
    private AutoSetObj autoSetObj4;

    @AutoSet("globalName")
    private String globalSet;

    @AutoSet
    Random random;

    @AutoSet
    ProxyUnit proxyUnit;

    @AutoSet
    private static TypeTo typeTo;

    @PortIn(value = "say", nece = {"name", "age"})
    public Object say(OftenObject oftenObject)
    {
        Assert.assertEquals(AspectHandle.class,oftenObject.getRequestData("aspect-handle"));
        int age = (int) oftenObject._fn[1];
        return oftenObject._fn[0] + "+" + age;
    }

    @PortIn(tiedType = TiedType.METHOD, nece = {"sex"}, method = PortMethod.POST)
    @Parse(paramNames = "sex", parser = IntParser.class)
    public Object sayHelloPost(OftenObject oftenObject)
    {
        int sex = (int) oftenObject._fn[0];
        return oftenObject.funTied() + ":" + sex;
    }

    @PortIn(tiedType = TiedType.METHOD, nece = {"sex"})
    public Object sayHello(OftenObject oftenObject)
    {
        String sex = (String) oftenObject._fn[0];
        return oftenObject.funTied() + "=" + sex;
    }

    @PortIn("parseObject")
    @Parse(paramNames = "myAge", parser = IntParser.class)
    @BindEntities({Article.class})
    public Object parseObject(OftenObject oftenObject)
    {
        Article article = oftenObject.fentity(0);
        User user = oftenObject.centity(0);
        // LOGGER.debug("{}\n{}", article, user);

        return random.nextBoolean() ? user : article;
    }


    @PortStart
    @LogMethodInvoke
    public void onStart()
    {
        try
        {
            proxyUnit.test();
            LOGGER.debug("{},{},{},{},{}", autoSetObj, autoSetObj2, autoSetObj3,autoSetObj4, globalSet);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "TypeTo转换");
            jsonObject.put("myAge", "19");
            typeTo.bindFieldParser(User.class, "myAge", new IntParser());
            User user = typeTo.parse(User.class, jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "TypeTo转换");
            jsonObject.put("myAge", "190");

            Person person = typeTo.parse(Person.class, jsonObject);

            LOGGER.debug("{},{}", user, person);
        } catch (Exception e)
        {
            LogUtil.printErrPosLn(e);
        }

    }

    @PortDestroy
    public void onDestroy()
    {
        LOGGER.debug("");
    }

}
