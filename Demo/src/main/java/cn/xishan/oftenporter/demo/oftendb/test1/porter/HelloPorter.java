package cn.xishan.oftenporter.demo.oftendb.test1.porter;

import java.util.Date;

import cn.xishan.oftenporter.demo.oftendb.test1.entity.Hello;
import cn.xishan.oftenporter.demo.oftendb.test1.unit.HelloUnit;
import cn.xishan.oftenporter.oftendb.annotation.TransactionJDBC;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.annotation.param.BindEntities;
import cn.xishan.oftenporter.porter.core.annotation.param.Parse;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.IdGen;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;
import cn.xishan.oftenporter.porter.core.util.LogMethodInvoke;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.simple.parsers.StringArrayParser;


@PortIn
public class HelloPorter
{

    @AutoSet
    HelloUnit helloUnit;

    @PortIn(inner = {"time", "_id"},
            method = PortMethod.POST)
    @BindEntities(Hello.class)
    public void add(WObject wObject)
    {
        Hello hello = wObject.fentity(0);
        wObject.finner[0] = new Date();
        wObject.finner[1] = KeyUtil.randomUUID();
        helloUnit.add(hello);
    }


    @PortIn(nece = {"name"})
    public void del(WObject wObject)
    {
        String name = wObject.fnOf(0);
        helloUnit.deleteByName(name);
    }

    @PortIn(nece = {"name", "newName"}, tieds = {"update", "update2"}, methods = {PortMethod.PUT, PortMethod.POST})
    public void update(WObject wObject)
    {
        String name = wObject.fnOf(0);
        String newName = wObject.fnOf(1);
        helloUnit.updateName(name, newName);
    }

    @PortIn(nece = {"name"})
    public Object count(WObject wObject)
    {
        String name = wObject.fnOf(0);
        return helloUnit.count(name);
    }


    @PortIn
    public Object list(WObject wObject)
    {
        return helloUnit.listAll();
    }


    @PortIn
    public void clear(WObject wObject)
    {
        helloUnit.clearAll();
    }


    @PortIn(nece = "names")
    @Parse(paramNames = "names", parser = StringArrayParser.class)
    @TransactionJDBC
    public Object transactionFailed(WObject wObject)
    {
        String[] names = wObject.fnOf(0);
        for (int i = 0; i < names.length; i++)
        {
            String name = names[i];
            helloUnit.deleteByName(name);
        }
        throw new RuntimeException("test transaction failed!");
    }

    @PortIn.PortStart
    @LogMethodInvoke
    public void onStart()
    {
        helloUnit.initTable();
    }

    @PortIn
    public void testSavePoint(WObject wObject)
    {
        _testSavePoint(wObject);
        LogUtil.printErrPos(helloUnit.listAll());
    }

    @TransactionJDBC
    public void _testSavePoint(WObject wObject)
    {
        helloUnit.clearAll();
        Hello hello = new Hello();
        hello.setId("1");
        hello.setName("h1");
        helloUnit.add(hello);

        hello = new Hello();
        hello.setId("2");
        hello.setName("h2");

        try
        {
            helloUnit.addHasSavePoint(hello,false);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        hello = new Hello();
        hello.setId("3");
        hello.setName("h3");

        try
        {
            helloUnit.addHasSavePoint(hello,true);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        hello = new Hello();
        hello.setId("4");
        hello.setName("h4");
        helloUnit.add(hello);
    }


}
