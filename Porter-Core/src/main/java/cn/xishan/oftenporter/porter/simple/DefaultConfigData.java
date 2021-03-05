package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.Property;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.exception.InitException;
import cn.xishan.oftenporter.porter.core.init.DealSharpProperties;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.OftenStrUtil;
import cn.xishan.oftenporter.porter.core.util.config.ChangeableProperty;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class DefaultConfigData implements IConfigData, IAttrGetter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfigData.class);

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

    static class Wrapper
    {
        final Class type;
        final OnValueChange change;
        final String[] attrs;
        final Object defaultVal;
        final Property.Choice choice;

        public Wrapper(Class type, Object defaultVal, Property.Choice choice, OnValueChange change, String[] attrs)
        {
            this.type = type;
            this.defaultVal = defaultVal;
            this.choice = choice;
            this.change = change;
            this.attrs = attrs;
        }
    }

    //private List<Wrapper> wrappers = new Vector<>();
    private Map<String, List<Wrapper>> attr2Wrappers = new Hashtable<>();
    private Map<String, Integer> attr2Count = new Hashtable<>();
    private Properties properties;

    public DefaultConfigData(Properties properties)
    {
        this.properties = new Properties();
        if (properties != null)
        {
            this.properties.putAll(properties);
        }
    }

    @Override
    public synchronized <T> void addOnValueChange(Class<T> type, T defaultVal, Property.Choice choice,
            OnValueChange<T> change, String... attrs)
    {
        if (type == null)
        {
            throw new NullPointerException("expected type");
        } else if (change == null)
        {
            throw new NullPointerException("expected change listener");
        }

        if (OftenTool.notEmptyOf(attrs))
        {
            Wrapper wrapper = new Wrapper(type, defaultVal, choice, change, attrs);

            for (String attr : attrs)
            {
                if (OftenTool.notEmpty(attr))
                {
                    List<Wrapper> list = attr2Wrappers.computeIfAbsent(attr, k -> new Vector<>());
                    list.add(wrapper);

                    if (attr2Count.containsKey(attr))
                    {
                        attr2Count.put(attr, attr2Count.get(attr) + 1);
                    } else
                    {
                        attr2Count.put(attr, 1);
                    }
                }
            }

        }
    }

    @Override
    public synchronized void removeOnValueChange(OnValueChange change)
    {
        Iterator<List<Wrapper>> it1 = attr2Wrappers.values().iterator();
        while (it1.hasNext())
        {
            List<Wrapper> list = it1.next();
            Iterator<Wrapper> iterator = list.iterator();
            while (iterator.hasNext())
            {
                Wrapper wrapper = iterator.next();
                if (wrapper.change == change)
                {
                    for (String attr : wrapper.attrs)
                    {
                        if (attr2Count.containsKey(attr))
                        {
                            int count = attr2Count.get(attr);
                            if (--count <= 0)
                            {
                                attr2Count.remove(attr);
                            }
                        }
                    }

                    iterator.remove();
                }
            }
            if (list.isEmpty())
            {
                it1.remove();
            }
        }

    }

    private void onChange(Properties newProps, Properties oldProps, Set<String> changeKeys)
    {
        if (attr2Wrappers.size() == 0)
        {
            return;
        }

        boolean addKey = false;
        if (changeKeys == null)
        {
            addKey = true;
            changeKeys = new HashSet<>();
        }
        DefaultConfigData oldData = new DefaultConfigData(oldProps);

        JSONObject newConfig = new JSONObject();
        for (Map.Entry entry : newProps.entrySet())
        {
            String key = String.valueOf(entry.getKey());
            if (addKey)
            {
                changeKeys.add(key);
            }

            newConfig.put(key, entry.getValue());
        }

        JSONObject oldConfig = new JSONObject();
        for (Map.Entry entry : oldProps.entrySet())
        {
            String key = String.valueOf(entry.getKey());
            if (addKey)
            {
                changeKeys.add(key);
            }
            oldConfig.put(key, entry.getValue());
        }

        for (String attr : changeKeys)
        {
            List<Wrapper> wrappers = attr2Wrappers.get(attr);
            if (wrappers != null)
            {
                for (Wrapper wrapper : wrappers)
                {
                    try
                    {
                        Object oldValue = oldData.getProperty(wrapper.type, attr, wrapper.defaultVal, wrapper.choice);
                        Object newValue = this.getProperty(wrapper.type, attr, wrapper.defaultVal, wrapper.choice);

//                        Object oldValue = oldConfig.getObject(attr, wrapper.type);
//                        Object newValue = newConfig.getObject(attr, wrapper.type);
                        if (!OftenTool.isEqual(newValue, oldValue))
                        {
                            wrapper.change.onChange(attr, newValue, oldValue);
                        }
                    } catch (Exception e)
                    {
                        LOGGER.warn("error for:change={},attr={},type={}", wrapper.change, attr, wrapper.type);
                        LOGGER.warn(e.getMessage(), e);
                    }
                }
            }
        }

    }

    @Override
    public Set<String> propertyNames()
    {
        return properties.stringPropertyNames();
    }

    @Override
    public JSONObject getJSONByKeyPrefix(String keyPrefix, boolean withPrefix)
    {
        JSONObject jsonObject = new JSONObject();
        for (String propName : propertyNames())
        {
            if (propName.startsWith(keyPrefix))
            {
                if (withPrefix)
                {
                    jsonObject.put(propName, get(propName));
                } else
                {
                    jsonObject.put(propName.substring(keyPrefix.length()), get(propName));
                }
            }
        }
        return jsonObject;
    }

    /**
     * @return 不含key前缀。
     */
    public static JSONObject getJSONByKeyPrefix(Properties properties, String keyPrefix)
    {
        return getJSONByKeyPrefix(properties, keyPrefix, false);
    }

    public static JSONObject getJSONByKeyPrefix(Properties properties, String keyPrefix, boolean withPrefix)
    {
        IAttrGetter getter = new IAttrGetter()
        {
            @Override
            public <T> T get(String name)
            {
                return (T) properties.get(name);
            }
        };
        return getJSONByKeyPrefix(getter, properties.stringPropertyNames(), keyPrefix, withPrefix);
    }

    private static JSONObject getJSONByKeyPrefix(IAttrGetter getter, Set<String> names, String keyPrefix,
            boolean withPrefix)
    {
        JSONObject jsonObject = new JSONObject();
        for (String propName : names)
        {
            if (propName.startsWith(keyPrefix))
            {
                if (withPrefix)
                {
                    jsonObject.put(propName, getter.get(propName));
                } else
                {
                    jsonObject.put(propName.substring(keyPrefix.length()), getter.get(propName));
                }
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
        return toStrings(str);
    }


    @Override
    public Set<String> getStringSet(String key)
    {
        return getStringSet(key, null);
    }

    @Override
    public Set<String> getStringSet(String key, String defaultValue)
    {
        String[] strs = getStrings(key, defaultValue);
        Set<String> set = null;
        if (strs != null)
        {
            set = new HashSet<>(strs.length);
            OftenTool.addAll(set, strs);
        }
        return set;
    }

    private String[] toStrings(String str)
    {
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

    public static <T> Set<T> asSet(T... elems)
    {
        Set<T> set = new HashSet<>();
        OftenTool.addAll(set, elems);
        return set;
    }

    @Override
    public <T> T set(String key, Object object)
    {
        Properties old = null;
        if (attr2Count.containsKey(key))
        {
            old = new Properties();
            old.putAll(properties);
        }

        Object rs = properties.put(key, object);

        if (old != null)
        {
            onChange(properties, old, asSet(key));
        }

        return (T) rs;
    }

    @Override
    public <T> T remove(String key)
    {
        Properties old = null;
        if (attr2Count.containsKey(key))
        {
            old = new Properties();
            old.putAll(properties);
        }

        T t = (T) properties.remove(key);

        if (old != null)
        {
            onChange(properties, old, asSet(key));
        }
        return t;
    }

    @Override
    public void putAll(Map<?, ?> map)
    {
        Properties old = null;
        Set<String> set = null;
        for (Object key : map.keySet())
        {
            String keyStr = String.valueOf(key);
            if (attr2Count.containsKey(keyStr))
            {
                if (old == null)
                {
                    old = new Properties();
                    old.putAll(properties);
                    set = new HashSet<>();
                }
                set.add(keyStr);
            }
        }

        if (properties.size() > 0)
        {
            HashMap map2 = new HashMap();
            map2.putAll(map);
            DealSharpProperties.dealSharpProperties(map2, properties, true);
            map = map2;
        }
        properties.putAll(map);

        if (old != null)
        {
            onChange(properties, old, set);
        }
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
    public Object getValue(Object object, Class currentObjectClass, Object target, Integer paramIndex,
            Class<?> fieldRealType, Property property)
    {
        if (!(target instanceof Field || target instanceof Method))
        {
            throw new IllegalArgumentException("expected field or method for target parameter");
        }

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
        boolean isChangeableProperty = false;
        if (OftenTool.subclassOf(fieldRealType, ChangeableProperty.class) == 0)
        {
            isChangeableProperty = true;
            if (target instanceof Field)
            {
                fieldRealType = AnnoUtil.Advance.getRealTypeInField(currentObjectClass, (Field) target);
            } else
            {
                fieldRealType = AnnoUtil.Advance
                        .getRealTypeInMethodParameter(currentObjectClass, (Method) target, paramIndex);
            }
        }

        String attrText = null;
        if (keys.length == 1)
        {
            attrText = keys[0];
            rs = getProperty(fieldRealType, keys[0], defaultVal, choice);
        } else
        {
            for (String key : keys)
            {
                rs = getProperty(fieldRealType, key, null, choice);
                if (OftenTool.isNullOrEmptyCharSequence(rs))
                {
                    attrText = key;
                    break;
                }
            }

            if (OftenTool.notEmptyOf(keys) && OftenTool.notEmpty(defaultVal))
            {
                attrText = keys[0];
                rs = getProperty(fieldRealType, keys[0], defaultVal, choice);
            }
        }

        LOGGER.debug("get prop:name={},property={},value={}", name, property, rs);

        if (isChangeableProperty)
        {
            OnValueChange[] onValueChanges = new OnValueChange[1];
            ChangeableProperty<Object> changeableProperty = new ChangeableProperty<Object>(attrText, rs)
            {
                @Override
                public void release()
                {
                    removeOnValueChange(onValueChanges[0]);
                    super.release();
                }
            };
            onValueChanges[0] = (attr, newValue, oldValue) -> {
                try
                {
                    changeableProperty.setAttr(attr);
                    changeableProperty.submitValue(newValue);
                } catch (Exception e)
                {
                    LOGGER.warn(e.getMessage(), e);
                }
            };
            addOnValueChange((Class<Object>) fieldRealType, defaultVal, choice, onValueChanges[0], keys);
            rs = changeableProperty;
        } else
        {
            if (target instanceof Field)
            {
                WeakReference<Field> fieldRef = new WeakReference<>((Field) target);
                WeakReference<Object> objRef = new WeakReference<>(object);

                addOnValueChange((Class<Object>) fieldRealType, defaultVal, choice, (attr, newValue, oldValue) -> {
                    try
                    {
                        Field f = fieldRef.get();
                        Object obj = objRef.get();

                        if (f == null || obj == null && !Modifier.isStatic(f.getModifiers()))
                        {
                            LOGGER.info("field is released:attr={},new={},old={}", attr, newValue, oldValue);
                        } else
                        {
                            f.set(obj, newValue);
                        }
                    } catch (Exception e)
                    {
                        LOGGER.warn(e.getMessage(), e);
                    }
                }, keys);
            }
        }

        return rs;
    }

    @Override
    public Object getValue(Object object, Class currentObjectClass, Object target, Integer paramIndex,
            Class<?> realType, String key, Object defaultValue)
    {
        boolean isChangeableProperty = false;
        if (OftenTool.subclassOf(realType, ChangeableProperty.class) == 0)
        {
            isChangeableProperty = true;
            if (target instanceof Field)
            {
                realType = AnnoUtil.Advance.getRealTypeInField(currentObjectClass, (Field) target);
            } else
            {
                realType = AnnoUtil.Advance
                        .getRealTypeInMethodParameter(currentObjectClass, (Method) target, paramIndex);
            }
        }

        Object value = getProperty(realType, key, null, Property.Choice.Default);
        if (OftenTool.isNullOrEmptyCharSequence(value))
        {
            value = defaultValue;
        }

        if (isChangeableProperty)
        {
            OnValueChange[] onValueChanges = new OnValueChange[1];
            ChangeableProperty<Object> changeableProperty = new ChangeableProperty<Object>(key, value)
            {
                @Override
                public void release()
                {
                    removeOnValueChange(onValueChanges[0]);
                    super.release();
                }
            };
            onValueChanges[0] = (attr, newValue, oldValue) -> {
                try
                {
                    changeableProperty.submitValue(newValue);
                } catch (Exception e)
                {
                    LOGGER.warn(e.getMessage(), e);
                }
            };
            addOnValueChange((Class<Object>) realType, defaultValue, null, onValueChanges[0], key);
            value = changeableProperty;
        }


        return value;
    }

    private Object getProperty(Class<?> fieldRealType, String key, Object defaultVal, Property.Choice choice)
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
            rs = getBoolean(key, defaultVal != null && TypeUtils.castToBoolean(defaultVal));
        } else if (fieldRealType.equals(float.class) || fieldRealType.equals(Float.class))
        {
            rs = getFloat(key, defaultVal == null ? 0 : TypeUtils.castToFloat(defaultVal));
        } else if (fieldRealType.equals(double.class) || fieldRealType.equals(Double.class))
        {
            rs = getDouble(key, defaultVal == null ? 0 : TypeUtils.castToDouble(defaultVal));
        } else if (fieldRealType.equals(String.class))
        {
            rs = getString(key, defaultVal == null ? null : defaultVal.toString());
        } else if (fieldRealType.equals(String[].class))
        {
            rs = getStrings(key, defaultVal == null ? null : defaultVal.toString());
        } else if (fieldRealType.equals(Set.class))
        {
            rs = getStringSet(key, defaultVal == null ? null : defaultVal.toString());
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
                    json = toJSON(defaultVal);
                }
            } else
            {
                json = getJSON(key);
                if (json == null && defaultVal != null)
                {
                    json = toJSON(defaultVal);
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
                    jsonArray = toArray(defaultVal);
                }
            } else
            {
                jsonArray = getJSONArray(key);
                if (jsonArray == null && defaultVal != null)
                {
                    jsonArray = toArray(defaultVal);
                }
            }
            rs = jsonArray;
        } else
        {
            rs = get(key);
        }

        if (rs != null && choice != null)
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

    private JSONObject toJSON(Object value)
    {
        JSONObject json = null;
        if (value instanceof JSONObject)
        {
            json = (JSONObject) value;
        } else if (value instanceof Map)
        {
            json = new JSONObject((Map) value);
        } else if (value != null)
        {
            json = JSON.parseObject(String.valueOf(value));
        }
        return json;
    }

    private JSONArray toArray(Object value)
    {
        JSONArray array = null;
        if (value instanceof JSONArray)
        {
            array = (JSONArray) value;
        } else if (value instanceof Collection)
        {
            array = new JSONArray();
            array.addAll((Collection) value);
        } else if (value != null)
        {
            array = JSON.parseArray(String.valueOf(value));
        }
        return array;
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
