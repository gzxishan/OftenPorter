package cn.xishan.oftenporter.demo.oftendb.test1.porter;

import java.util.Date;

import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.data.common;
import cn.xishan.oftenporter.oftendb.db.CUnit;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.NameValues;
import cn.xishan.oftenporter.oftendb.jbatis.JS;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.Parser;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.simple.parsers.JSONArrayParser;
import com.alibaba.fastjson.JSONArray;


@PortIn
public class Hello1Porter
{

    @AutoSet
    DBUnit dbUnit;

    @AutoSet
    JS js;

    @AutoSet
    private DBSource source;

    @PortIn(nece = {"name", "age", "sex"}, inner = {"time", "_id"},
            method = PortMethod.POST)
    @PortIn.Filter(
            before = @PortIn.Before(funTied = "addBefore", method = PortMethod.POST),
            after = @PortIn.After(funTied = "addAfter", method = PortMethod.POST)
    )
    public Object add(WObject wObject)
    {
        wObject.finner[0] = new Date();
        wObject.finner[1] = KeyUtil.randomUUID();
        return common.addData(wObject, source, false);
    }

    @PortIn(nece = {"name", "age", "sex"}, inner = {"time", "_id"},
            method = PortMethod.POST)
    public Object addBefore(WObject wObject)
    {
        wObject.fn[0] += "-before";
        wObject.finner[0] = new Date();
        wObject.finner[1] = KeyUtil.randomUUID();
        return common.addData(wObject, source, false);
    }

    @PortIn(nece = {"name", "age", "sex"}, inner = {"time", "_id"},
            method = PortMethod.POST)
    public Object addAfter(WObject wObject)
    {
        wObject.fn[0] += "-after";
        wObject.finner[0] = new Date();
        wObject.finner[1] = KeyUtil.randomUUID();
        return common.addData(wObject, source, false);
    }

    @PortIn(nece = {"name"})
    public Object del(WObject wObject)
    {
        Condition condition = source.newCondition();
        condition.append(Condition.EQ, new CUnit("name", wObject.fn[0]));
        return common.deleteData(wObject, source, condition);
    }

    @PortIn(nece = {"name"}, tieds = {"update", "update2"}, methods = {PortMethod.PUT, PortMethod.POST})
    public Object update(WObject wObject)
    {
        Condition condition = source.newCondition();
        condition.append(Condition.EQ, new CUnit("name", wObject.fn[0]));
        NameValues nameValues = new NameValues();
        nameValues.append("time", new Date());
        return common.updateData(wObject, source, condition, nameValues);
    }

    @PortIn(nece = {"name"})
    public Object count(WObject wObject)
    {
        return common.count(wObject, source, "name", wObject.fn[0]);
    }

    @PortIn
    public void testJBatis(WObject wObject)
    {
        LogUtil.printErrPos(dbUnit.add(wObject));
    }

    @PortIn
    public Object list(WObject wObject)
    {
        return common.queryData(wObject, source, null, null, null);
    }

    @PortIn(nece = "names")
    @Parser.parse(paramNames = "names", parser = JSONArrayParser.class)
    public Object transactionOk(WObject wObject)
    {
        JSONArray names = wObject.fnOf(0);
        dbUnit.add(wObject);
        for (int i = 0; i < names.size(); i++)
        {
            Condition condition = source.newCondition();
            condition.append(Condition.EQ, new CUnit("name", names.get(i)));
            JResponse jResponse = common.deleteData(wObject, source, condition);
            jResponse.throwExCause();
        }
        return new JResponse(ResultCode.SUCCESS);
    }

    @PortIn
    public Object clear(WObject wObject)
    {
        return common.deleteData(wObject, source, null);
    }


    @PortIn(nece = "names")
    @Parser.parse(paramNames = "names", parser = JSONArrayParser.class)
    public Object transactionFailed(WObject wObject)
    {
        JSONArray names = wObject.fnOf(0);
        dbUnit.add(wObject);
        for (int i = 0; i < names.size(); i++)
        {
            Condition condition = source.newCondition();
            condition.append(Condition.EQ, new CUnit("name", names.get(i)));
            JResponse jResponse = common.deleteData(wObject, source, condition);
            jResponse.throwExCause();
        }
        throw new RuntimeException("test transaction failed!");
    }

    @PortIn.PortStart
    public void onStart()
    {
        js.call("**********************\n","starting...");
        js.callMethod("saySth", "HelloWorld","\n********************************\n");
    }


}
