package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.MayProxyObject;
import cn.xishan.oftenporter.porter.core.annotation.Property;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;
import cn.xishan.oftenporter.porter.core.init.DealSharpProperties;
import cn.xishan.oftenporter.porter.simple.DefaultConfigData;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * 可以通过@{@linkplain AutoSet}引入该对象实例,见{@linkplain DefaultConfigData}。
 * <p>
 * 支持替换参数:#{properName}。如app.lib=#{basedir}/lib。见{@linkplain DealSharpProperties}。
 * 另外高级注解支持${properName}参数，见{@linkplain AnnoUtil}。
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public interface IConfigData
{
    interface OnValueChange<T>
    {
        void onChange(String attr, T newValue, T oldValue) throws Exception;
    }

    <T> void addOnValueChange(Class<T> type, OnValueChange<T> change, String... attrs);

    void removeOnValueChange(OnValueChange change);

    boolean contains(String key);

    long getLong(String key);

    long getLong(String key, long defaultValue);

    int getInt(String key);

    int getInt(String key, int defaultValue);

    float getFloat(String key);

    float getFloat(String key, float defaultValue);

    double getDouble(String key);

    double getDouble(String key, double defaultValue);

    boolean getBoolean(String key);

    boolean getBoolean(String key, boolean defaultValue);

    String getString(String key);

    String getString(String key, String defaultValue);

    String[] getStrings(String key);

    String[] getStrings(String key, String defaultValue);

    Date getDate(String key);

    Date getDate(String key, Date defaultValue);

    Date getDate(String key, String format);

    Date getDate(String key, String format, Date defaultValue);

    JSONObject getJSON(String key);

    JSONArray getJSONArray(String key);

    /**
     * 获得所有属性名。
     *
     * @return
     */
    Set<String> propertyNames();

    /**
     * 从key前缀获取json对象。
     *
     * @param keyPrefix
     * @return
     */
    JSONObject getJSONByKeyPrefix(String keyPrefix);

    /**
     * @param key
     * @param object
     * @param <T>
     * @return 上一个对象
     */
    <T> T set(String key, Object object);

    /**
     * 移除配置。
     *
     * @param key
     * @return 上一个对象
     */
    <T> T remove(String key);

    void putAll(Map<?, ?> map);

    /**
     * 见{@linkplain #set(String, Object)}.
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> T get(String key);

    /**
     * @param object
     * @param target   Method或Field
     * @param realType
     * @param property
     * @return
     */
    Object getValue(@MayNull @MayProxyObject Object object, Object target, Class<?> realType, Property property);

    /**
     * @param object
     * @param target   Method或Field
     * @param realType
     * @param key
     * @return
     */
    Object getValue(@MayNull @MayProxyObject Object object, Object target, Class<?> realType, String key,
            Object defaultValue);
}
