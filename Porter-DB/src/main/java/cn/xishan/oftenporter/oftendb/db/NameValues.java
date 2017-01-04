package cn.xishan.oftenporter.oftendb.db;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来说明哪些字段有效或无效。
 *
 * @author ZhuiFeng
 */
public class NameValues
{

    public interface Foreach
    {
        /**
         * @param name
         * @param value
         * @return 返回false表示中断循环.
         */
        public boolean forEach(String name, Object value);
    }

    // private Map<String, Object> map;
    private List<String> names;
    private List<Object> values;

    public NameValues()
    {
        names = new ArrayList<String>();
        values = new ArrayList<Object>();
    }

    public NameValues(int capacity)
    {
        names = new ArrayList<String>(capacity);
        values = new ArrayList<Object>(capacity);
    }

    public NameValues put(String name, Object value)
    {
        names.add(name);
        values.add(value);
        return this;
    }

    public String[] names()
    {
        return names.toArray(new String[0]);
    }

    public void forEach(Foreach foreach)
    {
        for (int i = 0; i < names.size(); i++)
        {
            if (!foreach.forEach(names.get(i), values.get(i)))
            {
                break;
            }
        }

    }

    public void clear()
    {
        names.clear();
        values.clear();
    }

    public Object value(int index)
    {
        return values.get(index);
    }

    public String name(int index)
    {
        return names.get(index);
    }

    public int size()
    {
        return names.size();
    }

}