package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.Property;
import cn.xishan.oftenporter.porter.core.util.StrUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;

import java.util.Date;
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
        if (properties == null)
        {
            properties = new Properties();
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
        return getLong(key, 0L);
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
        return getInt(key, 0);
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
        return getFloat(key, 0f);
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
    public double getDouble(String key)
    {
        return getDouble(key, 0d);
    }

    @Override
    public double getDouble(String key, double defaultValue)
    {
        String value = properties.getProperty(key);
        if (WPTool.isEmpty(value))
        {
            return defaultValue;
        }
        Double val = TypeUtils.castToDouble(value);
        return val == null ? 0 : val;
    }

    @Override
    public boolean getBoolean(String key)
    {
        return getBoolean(key, false);
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

    /**
     * 通过逗号隔开
     *
     * @param key
     * @return
     */
    @Override
    public String[] getStrings(String key)
    {
        return getStrings(key, null);
    }

    @Override
    public String[] getStrings(String key, String defaultValue)
    {
        String str = getString(key, defaultValue);
        if (WPTool.isEmpty(str))
        {
            return null;
        }
        return StrUtil.split(str.replace('，', ','), ",");
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

    @Override
    public Object getValue(Object object, Object target, Class<?> fieldRealType, Property property)
    {
        String[] keys = StrUtil.split(property.value().replace('，', ','), ",");

        String defaultVal = property.defaultVal().trim();
        if (defaultVal.equals(""))
        {
            defaultVal = null;
        }
        Object rs = null;
        if (keys.length == 1)
        {
            rs = getProperty(fieldRealType, keys[0], defaultVal);
        } else
        {
            for (String key : keys)
            {
                rs = getProperty(fieldRealType, key, null);
                if (WPTool.notNullAndEmpty(rs))
                {
                    break;
                }
            }
            if (WPTool.notNullAndEmptyForAll(keys, defaultVal))
            {
                rs = getProperty(fieldRealType, keys[0], defaultVal);
            }
        }
        return rs;
    }

    private Object getProperty(Class<?> fieldRealType, String key, String defaultVal)
    {
        if (fieldRealType.equals(int.class) || fieldRealType.equals(Integer.class))
        {
            return getInt(key, defaultVal == null ? 0 : TypeUtils.castToInt(defaultVal));
        } else if (fieldRealType.equals(long.class) || fieldRealType.equals(Long.class))
        {
            return getLong(key, defaultVal == null ? 0 : TypeUtils.castToLong(defaultVal));
        } else if (fieldRealType.equals(boolean.class) || fieldRealType.equals(Boolean.class))
        {
            return getBoolean(key, defaultVal == null ? false : TypeUtils.castToBoolean(defaultVal));
        } else if (fieldRealType.equals(float.class) || fieldRealType.equals(Float.class))
        {
            return getFloat(key, defaultVal == null ? 0 : TypeUtils.castToFloat(defaultVal));
        } else if (fieldRealType.equals(double.class) || fieldRealType.equals(Double.class))
        {
            return getDouble(key, defaultVal == null ? 0 : TypeUtils.castToDouble(defaultVal));
        } else if (fieldRealType.equals(String.class))
        {
            return getString(key, defaultVal);
        } else if (fieldRealType.equals(String[].class))
        {
            return getStrings(key, defaultVal);
        } else if (fieldRealType.equals(Date.class))
        {
            return getDate(key, defaultVal == null ? null : TypeUtils.castToDate(defaultVal));
        } else if (fieldRealType.equals(JSONObject.class))
        {
            JSONObject json = getJSON(key);
            if (json == null && defaultVal != null)
            {
                json = JSON.parseObject(defaultVal);
            }
            return json;
        } else if (fieldRealType.equals(JSONArray.class))
        {
            JSONArray jsonArray = getJSONArray(key);
            if (jsonArray == null && defaultVal != null)
            {
                jsonArray = JSON.parseArray(defaultVal);
            }
            return jsonArray;
        } else
        {
            return get(key);
        }
    }
}
