package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.util.OftenTool;
import com.alibaba.fastjson.JSONObject;


/**
 * @author Created by https://github.com/CLovinr on 2018-11-06.
 */
public class FunParam
{
    private String name;
    private Object value;

    public FunParam(String name, Object value)
    {
        this.name = name;
        this.value = value;
    }

    public FunParam()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public static JSONObject toJSON(FunParam[] funParams)
    {
        JSONObject jsonObject = new JSONObject();
        for (FunParam funParam : funParams)
        {
            jsonObject.put(funParam.getName(), funParam.getValue());
        }
        return jsonObject;
    }

    /**
     * 如果元素非为{@linkplain FunParam},则name为objects[i].getClass().getName().
     *
     * @param objects
     * @return
     */
    public static JSONObject toJSON(Object... objects)
    {
        JSONObject jsonObject = new JSONObject();
        for (Object obj : objects)
        {
            if (obj instanceof FunParam)
            {
                FunParam funParam = (FunParam) obj;
                if (OftenTool.isEmpty(funParam.getName()))
                {
                    throw new NullPointerException("empty FunParam name!");
                }
                jsonObject.put(funParam.getName(), funParam.getValue());
            } else if (obj != null)
            {
                jsonObject.put(obj.getClass().getName(), obj);
            }
        }
        return jsonObject;
    }
}
