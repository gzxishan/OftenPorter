package cn.xishan.oftenporter.porter.simple;


import cn.xishan.oftenporter.porter.core.base.INameValues;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefaultNameValues implements INameValues
{

    private List<String> names;
    private List<Object> values;


    /**
     * @param nameValues 必须是key(String),value(Object),key,value...的形式
     * @return
     */
    public static DefaultNameValues fromArray(Object... nameValues)
    {
        if (nameValues.length % 2 != 0)
        {
            throw new IllegalArgumentException("Illegal arguments length:" + nameValues.length);
        }
        String[] names = new String[nameValues.length / 2];
        Object[] values = new Object[nameValues.length / 2];
        for (int i = 0, k = 0; i < nameValues.length; i += 2, k++)
        {
            names[k] = (String) nameValues[i];
            values[k] = nameValues[i + 1];
        }
        DefaultNameValues defaultNameValues = new DefaultNameValues();
        defaultNameValues.append(names, values);
        return defaultNameValues;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject jsonObject = new JSONObject();
        if (names != null)
        {
            for (int i = 0; i < names.size(); i++)
            {
                jsonObject.put(names.get(i), values.get(i));
            }
        }
        return jsonObject;
    }


    /**
     * 提取键值对。
     *
     * @param jsonObject 要提取的json对象
     * @return 提取结果
     */
    public static DefaultNameValues fromJSON(JSONObject jsonObject)
    {

        String[] names = new String[jsonObject.size()];
        Object[] values = new Object[jsonObject.size()];
        Iterator<Map.Entry<String, Object>> iterator = jsonObject.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext())
        {
            Map.Entry<String, Object> entry = iterator.next();
            names[i] = entry.getKey();
            values[i++] = entry.getValue();
        }
        DefaultNameValues defaultNameValues = new DefaultNameValues();
        defaultNameValues.append(names, values);
        return defaultNameValues;
    }

    public DefaultNameValues append(INameValues INameValues)
    {

        if (INameValues != null)
        {
            String[] names = INameValues.getNames();
            Object[] values = INameValues.getValues();
            append(names, values);
        }
        return this;
    }

    public DefaultNameValues(List<String> names, List<Object> values)
    {
        this();
        append(names, values);
    }


    public DefaultNameValues(String[] names, Object[] values)
    {
        this();
        append(names, values);
    }

    public DefaultNameValues(int capacity)
    {
        names = new ArrayList<>(capacity);
        values = new ArrayList<>(capacity);
    }

    public DefaultNameValues()
    {
        names = new ArrayList<>();
        values = new ArrayList<>();
    }

    @Override
    public String[] getNames()
    {
        return names.toArray(OftenTool.EMPTY_STRING_ARRAY);
    }

    @Override
    public Object[] getValues()
    {
        return values.toArray(OftenTool.EMPTY_OBJECT_ARRAY);
    }


    public DefaultNameValues append(String name, Object value)
    {
        this.names.add(name);
        this.values.add(value);
        return this;
    }

    public DefaultNameValues append(List<String> names, List<Object> values)
    {
        return this.append(names.toArray(OftenTool.EMPTY_STRING_ARRAY), values.toArray(OftenTool.EMPTY_OBJECT_ARRAY));
    }

    public DefaultNameValues append(String[] names, Object[] values)
    {
        if (names.length != values.length)
        {
            throw new IllegalArgumentException("names's length not values's");
        }
        OftenTool.addAll(this.names, names);
        OftenTool.addAll(this.values, values);
        return this;
    }


}
