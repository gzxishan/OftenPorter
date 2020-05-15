package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.Property;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.DealSharpProperties;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class DefaultConfigData implements IConfigData
{
    private static class ArrayKey implements Comparable<ArrayKey>
    {
        private int index;
        private String key;
        private static final Pattern KEY_PATTERN = Pattern.compile("^\\[([0-9]+)\\]$");

        public ArrayKey(String keyStr)
        {
            keyStr = keyStr.trim();
            this.key = keyStr;
            Matcher matcher = KEY_PATTERN.matcher(keyStr);
            if (!matcher.find())
            {
                throw new InitException("illegal key for ArrayPrefix:key=" + keyStr);
            } else
            {
                this.index = Integer.parseInt(matcher.group(1));
            }
        }

        public int getIndex()
        {
            return index;
        }

        public String getKey()
        {
            return key;
        }

        @Override
        public int compareTo(ArrayKey o)
        {
            if (this.index > o.index)
            {
                return 1;
            } else if (this.index == o.index)
            {
                return 0;
            } else
            {
                return -1;
            }
        }
    }

    private Properties properties;
    //private Map<String, Object> data = new ConcurrentHashMap<>();

    public DefaultConfigData(Properties properties)
    {
        this.properties = new Properties();
        if (properties != null)
        {
            this.properties.putAll(properties);
        }
    }

    @Override
    public Set<String> propertyNames()
    {
        return properties.stringPropertyNames();
    }

    @Override
    public JSONObject getJSONByKeyPrefix(String keyPrefix)
    {
        JSONObject jsonObject = new JSONObject();
        for (String propName : propertyNames())
        {
            if (propName.startsWith(keyPrefix))
            {
                jsonObject.put(propName.substring(keyPrefix.length()), get(propName));
            }
        }
        return jsonObject;
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
        Object value = get(key);
        if (OftenTool.isNullOrEmptyCharSequence(value))
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
        Object value = get(key);
        if (OftenTool.isNullOrEmptyCharSequence(value))
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
        Object value = get(key);
        if (OftenTool.isNullOrEmptyCharSequence(value))
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
        Object value = get(key);
        if (OftenTool.isNullOrEmptyCharSequence(value))
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
        Object value = get(key);
        if (OftenTool.isNullOrEmptyCharSequence(value))
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
        Object value = get(key);
        if (OftenTool.isNullOrEmptyCharSequence(value))
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
        String[] strs = OftenStrUtil.split(str.replace('，', ','), ",");
        for (int i = 0; i < strs.length; i++)
        {
            strs[i] = strs[i].trim();
        }
        return strs;
    }

    @Override
    public Date getDate(String key)
    {
        Object value = get(key);
        if (OftenTool.isNullOrEmptyCharSequence(value))
        {
        }
        Date dateVal = TypeUtils.castToDate(value);
        return dateVal;
    }

    @Override
    public Date getDate(String key, Date defaultValue)
    {
        Object value = get(key);
        if (OftenTool.isNullOrEmptyCharSequence(value))
        {
            return defaultValue;
        }
        Date dateVal = TypeUtils.castToDate(value);
        return dateVal;
    }

    @Override
    public Date getDate(String key, String format)
    {
        Object value = get(key);
        Date dateVal = TypeUtils.castToDate(value, format);
        return dateVal;
    }

    @Override
    public Date getDate(String key, String format, Date defaultValue)
    {
        Object value = get(key);
        if (OftenTool.isNullOrEmptyCharSequence(value))
        {
            return defaultValue;
        }
        Date dateVal = TypeUtils.castToDate(value, format);
        return dateVal;
    }

    @Override
    public JSONObject getJSON(String key)
    {
        Object value = get(key);
        if (OftenTool.isNullOrEmptyCharSequence(value))
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
        Object value = get(key);
        if (OftenTool.isNullOrEmptyCharSequence(value))
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
        Object rs = properties.put(key, object);
        return (T) rs;
    }

    @Override
    public <T> T remove(String key)
    {
        return (T) properties.remove(key);
    }

    @Override
    public void putAll(Map<?, ?> map)
    {
        if (properties.size() > 0)
        {
            DealSharpProperties.dealSharpProperties(map, properties, true);
        }
        properties.putAll(map);
    }

    @Override
    public <T> T get(String key)
    {
        Object rs = properties.get(key);
        if (rs instanceof CharSequence)
        {
            rs = String.valueOf(rs).trim();
        }
        return (T) rs;
    }

    @Override
    public Object getValue(Object object, Object target, Class<?> fieldRealType, Property property)
    {
        String name = property.name();
        if (OftenTool.isEmpty(name))
        {
            name = property.value();
        }

        if (OftenTool.isEmpty(name))
        {
            throw new InitException(String.format("property name is empty:target=%s,type=%s", target, fieldRealType));
        }

        String[] keys = OftenStrUtil.split(name.replace('，', ','), ",");
        for (int i = 0; i < keys.length; i++)
        {
            String key = keys[i].trim();
            if (key.startsWith("${") && key.endsWith("}"))
            {
                key = key.substring(2, key.length() - 1);
            }
            keys[i] = key;
        }

        String defaultVal = property.defaultVal().trim();
        Property.Choice choice = property.choice();
        if (defaultVal.equals(""))
        {
            defaultVal = null;
        }
        Object rs = null;
        if (keys.length == 1)
        {
            rs = getProperty(fieldRealType, keys[0], defaultVal, choice);
        } else
        {
            for (String key : keys)
            {
                rs = getProperty(fieldRealType, key, null, choice);
                if (OftenTool.isNullOrEmptyCharSequence(rs))
                {
                    break;
                }
            }
            if (OftenTool.notEmptyOf(keys) && OftenTool.notEmpty(defaultVal))
            {
                rs = getProperty(fieldRealType, keys[0], defaultVal, choice);
            }
        }
        return rs;
    }

    private Object getProperty(Class<?> fieldRealType, String key, String defaultVal, Property.Choice choice)
    {
        Object rs;
        if (fieldRealType.equals(int.class) || fieldRealType.equals(Integer.class))
        {
            rs = getInt(key, defaultVal == null ? 0 : TypeUtils.castToInt(defaultVal));
        } else if (fieldRealType.equals(long.class) || fieldRealType.equals(Long.class))
        {
            rs = getLong(key, defaultVal == null ? 0 : TypeUtils.castToLong(defaultVal));
        } else if (fieldRealType.equals(boolean.class) || fieldRealType.equals(Boolean.class))
        {
            rs = getBoolean(key, defaultVal == null ? false : TypeUtils.castToBoolean(defaultVal));
        } else if (fieldRealType.equals(float.class) || fieldRealType.equals(Float.class))
        {
            rs = getFloat(key, defaultVal == null ? 0 : TypeUtils.castToFloat(defaultVal));
        } else if (fieldRealType.equals(double.class) || fieldRealType.equals(Double.class))
        {
            rs = getDouble(key, defaultVal == null ? 0 : TypeUtils.castToDouble(defaultVal));
        } else if (fieldRealType.equals(String.class))
        {
            rs = getString(key, defaultVal);
        } else if (fieldRealType.equals(String[].class))
        {
            rs = getStrings(key, defaultVal);
        } else if (fieldRealType.equals(Date.class))
        {
            rs = getDate(key, defaultVal == null ? null : TypeUtils.castToDate(defaultVal));
        } else if (fieldRealType.equals(JSONObject.class))
        {
            JSONObject json;
            if (choice == Property.Choice.JsonPrefix)
            {
                json = getJSONByKeyPrefix(key);
                if (json.isEmpty() && defaultVal != null)
                {
                    json = JSON.parseObject(defaultVal);
                }
            } else
            {
                json = getJSON(key);
                if (json == null && defaultVal != null)
                {
                    json = JSON.parseObject(defaultVal);
                }
            }
            rs = json;
        } else if (fieldRealType.equals(JSONArray.class))
        {
            JSONArray jsonArray;
            if (choice == Property.Choice.ArrayPrefix)
            {
                JSONObject json = getJSONByKeyPrefix(key);
                if (!json.isEmpty())
                {
                    List<ArrayKey> arrayKeyList = new ArrayList<>(json.size());
                    for (Map.Entry<String, Object> entry : json.entrySet())
                    {
                        arrayKeyList.add(new ArrayKey(entry.getKey()));
                    }
                    ArrayKey[] arrayKeys = arrayKeyList.toArray(new ArrayKey[0]);
                    Arrays.sort(arrayKeys);
                    jsonArray = new JSONArray(arrayKeys.length);
                    for (int i = 0; i < arrayKeys.length; i++)
                    {
                        jsonArray.add(json.get(arrayKeys[i].getKey()));
                    }
                } else
                {
                    jsonArray = JSON.parseArray(defaultVal);
                }
            } else
            {
                jsonArray = getJSONArray(key);
                if (jsonArray == null && defaultVal != null)
                {
                    jsonArray = JSON.parseArray(defaultVal);
                }
            }
            rs = jsonArray;
        } else
        {
            rs = get(key);
        }

        if (rs != null)
        {
            switch (choice)
            {
                case FirstFile:
                case FirstDir:
                case FirstFileDir:
                    rs = choiceFile(choice, rs, fieldRealType);
                    break;
                case Default:
                    break;
            }
        }

        return rs;
    }

    private Object choiceFile(Property.Choice choice, Object rs, Class realType)
    {
        if (rs instanceof CharSequence)
        {
            String str = String.valueOf(rs).replaceAll("[；,\\|]", ";");
            rs = OftenStrUtil.split(str, ";");
        }


        if (rs instanceof File)
        {
            rs = new File[]{(File) rs};
        }

        if (rs instanceof List || rs.getClass().isArray() && !(rs instanceof File[]))
        {
            if (rs.getClass().isArray())
            {
                int len = Array.getLength(rs);
                List list = new ArrayList(len);
                for (int i = 0; i < len; i++)
                {
                    list.add(Array.get(rs, i));
                }
                rs = list;
            }

            List list = (List) rs;
            List<File> fileList = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++)
            {
                Object item = list.get(i);
                if (item instanceof File)
                {
                    fileList.add((File) item);
                } else if (item instanceof CharSequence && OftenTool.notEmpty((CharSequence) item))
                {
                    fileList.add(new File(String.valueOf(item)));
                }
            }
            rs = fileList.toArray(new File[0]);
        }

        if (rs instanceof File[])
        {
            File[] files = (File[]) rs;
            File file = null;
            for (File f : files)
            {
                if (f.exists())
                {
                    if (choice == Property.Choice.FirstFileDir || choice == Property.Choice.FirstFile && f
                            .isFile() || choice == Property.Choice.FirstDir && f.isDirectory())
                    {
                        file = f;
                        break;
                    }
                }
            }
            if (file == null && files.length > 0)
            {
                file = files[0];//都不存在时，返回第一个文件
            }
            if (OftenTool.isAssignable(realType, String.class))
            {
                rs = file.getAbsolutePath();
            } else
            {
                rs = file;
            }
        } else
        {
            throw new InitException(String.format("unknown property for choice '%s':%s", choice, rs));
        }

        return rs;
    }
}
