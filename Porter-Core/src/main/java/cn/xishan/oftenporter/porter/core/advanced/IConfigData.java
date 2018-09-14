package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.MayNull;
import cn.xishan.oftenporter.porter.core.annotation.MayProxyObject;
import cn.xishan.oftenporter.porter.core.annotation.Property;
import cn.xishan.oftenporter.porter.simple.DefaultConfigData;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;

/**
 * 可以通过@{@linkplain AutoSet}引入该对象实例,见{@linkplain DefaultConfigData}。
 *
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public interface IConfigData
{
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
     * @param key
     * @param object
     * @param <T>
     * @return 上一个对象
     */
    <T> T set(String key, Object object);

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
}
