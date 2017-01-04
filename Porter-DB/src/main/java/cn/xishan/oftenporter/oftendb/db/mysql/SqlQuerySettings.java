package cn.xishan.oftenporter.oftendb.db.mysql;


import cn.xishan.oftenporter.oftendb.db.BaseEasier;
import cn.xishan.oftenporter.oftendb.db.QuerySettings;

import java.util.ArrayList;
import java.util.List;

public class SqlQuerySettings extends QuerySettings
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

    private static class FindOneSettings extends SqlQuerySettings
    {
        Object finalObject;

        public FindOneSettings()
        {
            setLimit(1);
            setSkip(0);
            finalObject = super.toFinalObject();
        }

        @Override
        public Object toFinalObject()
        {
            return finalObject;
        }
    }

    public static final SqlQuerySettings FIND_ONE = new FindOneSettings();

    @Override
    public QuerySettings putOrder(String name, int n)
    {
        list.add(new Temp(name, n));
        return this;
    }

    @Override
    public Object toFinalObject()
    {
        if (list.size() == 0)
        {
            return null;
        }
        StringBuilder sbuilder = new StringBuilder();

        for (int i = 0; i < list.size(); i++)
        {
            Temp temp = list.get(i);
            sbuilder.append('`').append(temp.name).append('`');
            if (temp.n == 1)
            {
                sbuilder.append(" ASC");
            } else
            {
                sbuilder.append(" DESC");
            }
            sbuilder.append(",");
        }
        if (sbuilder.length() > 0)
        {
            BaseEasier.removeEndChar(sbuilder, ',');
        }
        return sbuilder;
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
