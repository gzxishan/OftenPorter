package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class DefaultConfigData implements IConfigData
{
    private Properties properties;
    private Map<String, Object> data = new ConcurrentHashMap<>();

    public DefaultConfigData(Properties properties)
    {
        if(properties==null){
            properties=new Properties();
        }
        this.properties = properties;
    }

    @Override
    public boolean contains(String key)
    {
        return properties.containsKey(key);
    }

    @Override
    public long getLong(String key)
    {
        String value = properties.getProperty(key);
        Long longVal = TypeUtils.castToLong(value);
        return longVal == null ? 0L : longVal;
    }

    @Override
    public long getLong(String key, long defaultValue)
    {
        String value = properties.getProperty(key);
        if (WPTool.isEmpty(value))
        {
            return defaultValue;
        }
        Long longVal = TypeUtils.castToLong(value);
        return longVal == null ? 0L : longVal;
    }

    @Override
    public int getInt(String key)
    {
        String value = properties.getProperty(key);
        Integer intVal = TypeUtils.castToInt(value);
        return intVal == null ? 0 : intVal;
    }

    @Override
    public int getInt(String key, int defaultValue)
    {
        String value = properties.getProperty(key);
        if (WPTool.isEmpty(value))
        {
            return defaultValue;
        }
        Integer intVal = TypeUtils.castToInt(value);
        return intVal == null ? 0 : intVal;
    }

    @Override
    public float getFloat(String key)
    {
        String value = properties.getProperty(key);
        Float floatVal = TypeUtils.castToFloat(value);
        return floatVal == null ? 0 : floatVal;
    }

    @Override
    public float getFloat(String key, float defaultValue)
    {
        String value = properties.getProperty(key);
        if (WPTool.isEmpty(value))
        {
            return defaultValue;
        }
        Float floatVal = TypeUtils.castToFloat(value);
        return floatVal == null ? 0 : floatVal;
    }

    @Override
    public boolean getBoolean(String key)
    {
        String value = properties.getProperty(key);
        Boolean booleanVal = TypeUtils.castToBoolean(value);
        return booleanVal == null ? false : booleanVal;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue)
    {
        String value = properties.getProperty(key);
        if (WPTool.isEmpty(value))
        {
            return defaultValue;
        }
        Boolean booleanVal = TypeUtils.castToBoolean(value);
        return booleanVal == null ? false : booleanVal;
    }

    @Override
    public String getString(String key)
    {
        return properties.getProperty(key);
    }

    @Override
    public String getString(String key, String defaultValue)
    {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public Date getDate(String key)
    {
        String value = properties.getProperty(key);
        Date dateVal = TypeUtils.castToDate(value);
        return dateVal;
    }

    @Override
    public Date getDate(String key, Date defaultValue)
    {
        String value = properties.getProperty(key);
        if (WPTool.isEmpty(value))
        {
            return defaultValue;
        }
        Date dateVal = TypeUtils.castToDate(value);
        return dateVal;
    }

    @Override
    public Date getDate(String key, String format)
    {
        String value = properties.getProperty(key);
        Date dateVal = TypeUtils.castToDate(value, format);
        return dateVal;
    }

    @Override
    public Date getDate(String key, String format, Date defaultValue)
    {
        String value = properties.getProperty(key);
        if (WPTool.isEmpty(value))
        {
            return defaultValue;
        }
        Date dateVal = TypeUtils.castToDate(value, format);
        return dateVal;
    }

    @Override
    public JSONObject getJSON(String key)
    {
        String value = properties.getProperty(key);
        if (WPTool.isEmpty(value))
        {
            return null;
        }
        return JSON.parseObject(value);
    }

    @Override
    public JSONArray getJSONArray(String key)
    {
        String value = properties.getProperty(key);
        if (WPTool.isEmpty(value))
        {
            return null;
        }
        return JSON.parseArray(value);
    }

    @Override
    public <T> T set(String key, Object object)
    {
        Object rs = data.put(key, object);
        return (T) rs;
    }

    @Override
    public <T> T get(String key)
    {
        Object rs = data.get(key);
        if (rs == null)
        {
            rs = properties.getProperty(key);
        }
        return (T) rs;
    }
}
