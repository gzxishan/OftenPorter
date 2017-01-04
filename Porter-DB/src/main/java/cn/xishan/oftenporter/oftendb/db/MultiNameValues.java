package cn.xishan.oftenporter.oftendb.db;

import java.util.ArrayList;
import java.util.List;

public class MultiNameValues
{
    private String[] names;
    private List<Object[]> valueList;


    public MultiNameValues()
    {

    }

    public MultiNameValues names(String... names)
    {
        this.names = names;
        this.valueList = new ArrayList<Object[]>(names.length);
        return this;
    }

    public String[] getNames()
    {
        return names;
    }

    public int count()
    {
        return valueList.size();
    }

    public Object[] values(int index)
    {
        return valueList.get(index);
    }

    public MultiNameValues addValues(Object... values)
    {
        if (names == null)
        {
            throw new RuntimeException("names is null!");
        }
        valueList.add(values);
        return this;
    }

}
