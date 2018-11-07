package cn.xishan.oftenporter.porter.simple;


import cn.xishan.oftenporter.porter.core.base.INameValues;
import com.alibaba.fastjson.JSONObject;

import java.util.Iterator;
import java.util.Map;

public class DefaultNameValues implements INameValues
{

    private String[] names;
    private Object[] values;


    /**
     *
     * @param nameValues 必须是key(String),value(Object),key,value...的形式
     * @return
     */
    public static DefaultNameValues fromArray(Object ... nameValues){
        if (nameValues.length % 2 != 0)
        {
            throw new IllegalArgumentException("Illegal arguments length:" + nameValues.length);
        }
        DefaultNameValues appValues = new DefaultNameValues();
        String[] names = new String[nameValues.length / 2];
        Object[] values = new Object[nameValues.length / 2];
        for (int i = 0, k = 0; i < nameValues.length; i += 2, k++)
        {
            names[k] = (String) nameValues[i];
            values[k] = nameValues[i + 1];
        }
        appValues.names(names).values(values);
        return appValues;
    }

    public JSONObject toJson()
    {
        JSONObject jsonpObject = new JSONObject();
        if (names != null)
        {
            for (int i = 0; i < names.length; i++)
            {
                jsonpObject.put(names[i], values[i]);
            }
        }
        return jsonpObject;
    }


    /**
     * 提取键值对。
     *
     * @param jsonObject 要提取的json对象
     * @return 提取结果
     */
    public static DefaultNameValues fromJSON(JSONObject jsonObject)
    {
        DefaultNameValues defaultNameValues = new DefaultNameValues();

        String[] names = new String[jsonObject.size()];
        Object[] values = new Object[jsonObject.size()];
        defaultNameValues.names = names;
        defaultNameValues.values = values;

        Iterator<Map.Entry<String, Object>> iterator = jsonObject.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext())
        {
            Map.Entry<String, Object> entry = iterator.next();
            names[i] = entry.getKey();
            values[i++] = entry.getValue();
        }

        return defaultNameValues;
    }

    public DefaultNameValues add(INameValues INameValues)
    {

        if (INameValues != null)
        {
            String[] names = INameValues.getNames();
            Object[] values = INameValues.getValues();

            String[] strs;
            Object[] objects;

            int len1 = this.names == null ? 0 : this.names.length;
            int len2 = names == null ? 0 : names.length;
            strs = new String[len1 + len2];
            objects = new Object[strs.length];
            int offset = 0;
            if (len1 > 0)
            {
                System.arraycopy(this.names, 0, strs, offset, len1);
                System.arraycopy(this.values, 0, objects, offset, len1);
                offset += len1;
            }
            if (len2 > 0)
            {
                System.arraycopy(names, 0, strs, offset, len2);
                System.arraycopy(values, 0, objects, offset, len2);
            }
            this.names = strs;
            this.values = objects;
        }
        return this;
    }

    public DefaultNameValues(String... names)
    {
        names(names);
    }

    @Override
    public String[] getNames()
    {
        return names;
    }

    @Override
    public Object[] getValues()
    {
        return values;
    }


    /**
     * 设置值
     *
     * @param values 值列表
     * @return 返回自己
     */
    public DefaultNameValues values(Object... values)
    {
        this.values = values;
        return this;
    }

    /**
     * 设置名称
     *
     * @param names 名称列表
     * @return 返回自己
     */
    public DefaultNameValues names(String... names)
    {
        this.names = names;
        return this;
    }

}
