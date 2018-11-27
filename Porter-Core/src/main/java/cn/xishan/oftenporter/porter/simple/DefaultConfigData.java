package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.Property;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class DefaultConfigData implements IConfigData
{
    private Properties properties;
    //private Map<String, Object> data = new ConcurrentHashMap<>();

    public DefaultConfigData(Properties properties)
    {
        this.properties = new Properties();
        if(properties!=null){
            this.properties.putAll(properties);
        }
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
        Object value = properties.getProperty(key);
        if (OftenTool.isEmpty(value))
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
        Object value = properties.getProperty(key);
        if (OftenTool.isEmpty(value))
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
        Object value = properties.getProperty(key);
        if (OftenTool.isEmpty(value))
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
        Object value = properties.getProperty(key);
        if (OftenTool.isEmpty(value))
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
        Object value = properties.getProperty(key);
        if (OftenTool.isEmpty(value))
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
        Object value = properties.getProperty(key);
        if (OftenTool.isEmpty(value))
        {
            return defaultValue;
        }
        return String.valueOf(value);
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
        if (OftenTool.isEmpty(str))
        {
            return null;
        }
        return OftenStrUtil.split(str.replace('，', ','), ",");
    }

    @Override
    public Date getDate(String key)
    {
        Object value = properties.getProperty(key);
        if (OftenTool.isEmpty(value))
        {
        }
        Date dateVal = TypeUtils.castToDate(value);
        return dateVal;
    }

    @Override
    public Date getDate(String key, Date defaultValue)
    {
        Object value = properties.getProperty(key);
        if (OftenTool.isEmpty(value))
        {
            return defaultValue;
        }
        Date dateVal = TypeUtils.castToDate(value);
        return dateVal;
    }

    @Override
    public Date getDate(String key, String format)
    {
        Object value = properties.getProperty(key);
        Date dateVal = TypeUtils.castToDate(value, format);
        return dateVal;
    }

    @Override
    public Date getDate(String key, String format, Date defaultValue)
    {
        Object value = properties.getProperty(key);
        if (OftenTool.isEmpty(value))
        {
            return defaultValue;
        }
        Date dateVal = TypeUtils.castToDate(value, format);
        return dateVal;
    }

    @Override
    public JSONObject getJSON(String key)
    {
        Object value = properties.getProperty(key);
        if (OftenTool.isEmpty(value))
        {
            return null;
        } else if (value instanceof JSONObject)
        {
            return (JSONObject) value;
        }
        return JSON.parseObject(String.valueOf(value));
    }

    @Override
    public JSONArray getJSONArray(String key)
    {
        Object value = properties.getProperty(key);
        if (OftenTool.isEmpty(value))
        {
            return null;
        } else if (value instanceof JSONArray)
        {
            return (JSONArray) value;
        }
        return JSON.parseArray(String.valueOf(value));
    }

    @Override
    public <T> T set(String key, Object object)
    {
        Object rs = object == null ? properties.remove(key) : properties.put(key, object);
        return (T) rs;
    }

    @Override
    public void putAll(Map<?, ?> map)
    {
        properties.putAll(map);
    }

    @Override
    public <T> T get(String key)
    {
        Object rs = properties.get(key);
        return (T) rs;
    }

    @Override
    public Object getValue(Object object, Object target, Class<?> fieldRealType, Property property)
    {
        String[] keys = OftenStrUtil.split(property.value().replace('，', ','), ",");

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
                if (OftenTool.notNullAndEmpty(rs))
                {
                    break;
                }
            }
            if (OftenTool.notNullAndEmptyForAll(keys, defaultVal))
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
