package cn.xishan.oftenporter.porter.simple;


import cn.xishan.oftenporter.porter.core.base.AppValues;
import com.alibaba.fastjson.JSONObject;

import java.util.Iterator;
import java.util.Map;

public class SimpleAppValues implements AppValues
{

    private String[] names;
    private Object[] values;


    /**
     * 提取键值对。
     *
     * @param jsonObject 要提取的json对象
     * @return 提取结果
     */
    public static SimpleAppValues fromJSON(JSONObject jsonObject)
    {
        SimpleAppValues simpleAppValues = new SimpleAppValues();

        String[] names = new String[jsonObject.size()];
        Object[] values = new Object[jsonObject.size()];
        simpleAppValues.names=names;
        simpleAppValues.values=values;

        Iterator<Map.Entry<String,Object>> iterator = jsonObject.entrySet().iterator();
        int i=0;
        while (iterator.hasNext()){
            Map.Entry<String,Object> entry = iterator.next();
            names[i]=entry.getKey();
            values[i++]=entry.getValue();
        }

        return simpleAppValues;
    }

    public SimpleAppValues add(AppValues appValues)
    {

        if (appValues != null)
        {
            String[] names = appValues.getNames();
            Object[] values = appValues.getValues();

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

    public SimpleAppValues(String... names)
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
    public SimpleAppValues values(Object... values)
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
    public SimpleAppValues names(String... names)
    {
        this.names = names;
        return this;
    }

}
