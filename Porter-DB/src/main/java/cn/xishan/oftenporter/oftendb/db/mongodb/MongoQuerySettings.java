package cn.xishan.oftenporter.oftendb.db.mongodb;

import cn.xishan.oftenporter.oftendb.db.BaseEasier;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;

public class MongoQuerySettings extends QuerySettings
{


    private class Temp
    {
        String name;
        int n;

        public Temp(String name, int n)
        {
            this.name = name;
            this.n = n;
        }
    }

    private List<Temp> list = new ArrayList<Temp>();

    @Override
    public QuerySettings putOrder(String name, int n)
    {
        list.add(new Temp(name, n));
        return this;
    }

    @Override
    public Object toFinalObject()
    {
        DBObject dbObject = new BasicDBObject();
        for (int i = 0; i < list.size(); i++)
        {
            Temp temp = list.get(i);
            dbObject.put(temp.name, temp.n);
        }
        return dbObject;
    }

    @Override
    public void _dealNames(Class<?> c)
    {
        for (int i = 0; i < list.size(); i++)
        {
            Temp temp = list.get(i);
            temp.name = BaseEasier.dealWith_Key(c, temp.name);
        }
    }

}
