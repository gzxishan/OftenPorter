package cn.xishan.oftenporter.porter.local.porter;

import cn.xishan.oftenporter.porter.core.TypeTo;
import cn.xishan.oftenporter.porter.core.annotation.*;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.local.mixin.HelloMixinPorter;
import cn.xishan.oftenporter.porter.simple.parsers.IntParser;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by https://github.com/CLovinr on 2016/9/4.
 */
@Parser({
        @Parser.parse(paramNames = "age", parser = IntParser.class)
})
@PortIn(tied = "Hello", tiedType = TiedType.REST)
@PortInObj({User.class})
@Mixin({HelloMixinPorter.class})
public class HelloPorter extends SuperSetPorter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloPorter.class);


    @AutoSet
    private static AutoSetObj autoSetObj2;

    @AutoSet(range = AutoSet.Range.New)
    private AutoSetObj autoSetObj3;

    @AutoSet("globalName")
    private String globalSet;

    @AutoSet
    Random random;

    @AutoSet
    private static TypeTo typeTo;

    @PortIn(value = "say", nece = {"name", "age"})
    public Object say(WObject wObject)
    {
        int age = (int) wObject.fn[1];
        return wObject.fn[0] + "+" + age;
    }

    @PortIn(tiedType = TiedType.REST, nece = {"sex"}, method = PortMethod.POST)
    @Parser.parse(paramNames = "sex", parser = IntParser.class)
    public Object sayHelloPost(WObject wObject)
    {
        int sex = (int) wObject.fn[0];
        return wObject.restValue + ":" + sex;
    }

    @PortIn(tiedType = TiedType.REST, nece = {"sex"})
    public Object sayHello(WObject wObject)
    {
        String sex = (String) wObject.fn[0];
        return wObject.restValue + "=" + sex;
    }

    @PortIn("parseObject")
    @Parser.parse(paramNames = "myAge", parser = IntParser.class)
    @PortInObj({Article.class})
    public Object parseObject(WObject wObject)
    {
        Article article = wObject.finObject(0);
        User user = wObject.cinObject(0);
        // LOGGER.debug("{}\n{}", article, user);

        return random.nextBoolean() ? user : article;
    }


    @PortStart
    public void onStart()
    {
        try
        {
            LOGGER.debug("{},{},{},{}", autoSetObj, autoSetObj2, autoSetObj3, globalSet);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "TypeTo转换");
            jsonObject.put("myAge", "19");
            typeTo.bind(User.class, "myAge", new IntParser());
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
