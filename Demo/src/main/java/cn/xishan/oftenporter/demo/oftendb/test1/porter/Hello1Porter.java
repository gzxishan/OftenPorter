package cn.xishan.oftenporter.demo.oftendb.test1.porter;

import java.util.Date;

import cn.xishan.oftenporter.demo.oftendb.base.ParamsGetterImpl;
import cn.xishan.oftenporter.demo.oftendb.base.SqlDBSource;
import cn.xishan.oftenporter.oftendb.data.Common2;
import cn.xishan.oftenporter.oftendb.data.Common3;
import cn.xishan.oftenporter.oftendb.data.DataDynamic;
import cn.xishan.oftenporter.oftendb.data.DBSource;
import cn.xishan.oftenporter.oftendb.data.impl.DBSourceImpl;
import cn.xishan.oftenporter.oftendb.db.Condition;
import cn.xishan.oftenporter.oftendb.db.NameValues;
import cn.xishan.oftenporter.oftendb.db.Unit;
import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.ResultCode;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.Parser;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;
import cn.xishan.oftenporter.porter.simple.parsers.JSONArrayParser;
import com.alibaba.fastjson.JSONArray;


@PortIn
public class Hello1Porter
{
    private static class Source extends DBSourceImpl
    {

        public Source()
        {
            super(new ParamsGetterImpl().getParams(), new SqlDBSource());
        }
    }

    @AutoSet(classValue = Source.class)
    private DBSource source;

    @PortIn(nece = {"name", "age", "sex"}, inner = {"time", "_id"},
            method = PortMethod.POST)
    public Object add(WObject wObject)
    {
        wObject.finner[0] = new Date();
        wObject.finner[1] = KeyUtil.randomUUID();
        return Common2.C.addData(source, false, wObject);
    }

    @PortIn(nece = {"name"})
    public Object del(WObject wObject)
    {
        Condition condition = source.newCondition();
        condition.put(Condition.EQ, new Unit("name", wObject.fn[0]));
        return Common2.C.deleteData(source, condition, wObject);
    }

    @PortIn(nece = {"name"})
    public Object update(WObject wObject)
    {
        Condition condition = source.newCondition();
        condition.put(Condition.EQ, new Unit("name", wObject.fn[0]));
        NameValues nameValues = new NameValues();
        nameValues.put("time", new Date());
        return Common2.C.updateData(source, condition, nameValues, wObject);
    }

    @PortIn(nece = {"name"})
    public Object count(WObject wObject)
    {
        return Common2.C.exists(source, "name", wObject.fn[0], wObject);
    }

    @PortIn
    public Object list(WObject wObject)
    {
        return Common2.C.queryData(source, null, null, null, wObject);
    }

    @PortIn(nece = "names")
    @Parser.parse(varName = "names", parser = JSONArrayParser.class)
    public Object transactionOk(WObject wObject)
    {
        JSONArray names = wObject.fnOf(0);
        for (int i = 0; i < names.size(); i++)
        {
            Condition condition = source.newCondition();
            condition.put(Condition.EQ, new Unit("name", names.get(i)));
            JResponse jResponse = Common3.deleteData(condition, wObject);
            jResponse.throwExCause();
        }
        return new JResponse(ResultCode.SUCCESS);
    }

    @PortIn
    public Object clear(WObject wObject){
        return Common2.C.deleteData(source,null,wObject);
    }


    @PortIn(nece = "names")
    @Parser.parse(varName = "names", parser = JSONArrayParser.class)
    public Object transactionFailed(WObject wObject)
    {
        JSONArray names = wObject.fnOf(0);
        for (int i = 0; i < names.size(); i++)
        {
            Condition condition = source.newCondition();
            condition.put(Condition.EQ, new Unit("name", names.get(i)));
            JResponse jResponse = Common3.deleteData(condition, wObject);
            jResponse.throwExCause();
        }
        throw new RuntimeException("failed!");
    }


}
