package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.AutoSet;
import cn.xishan.oftenporter.porter.core.annotation.Property;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;

/**
 * 可以通过@{@linkplain AutoSet}引入该对象实例。
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

    float getDouble(String key);

    float getDouble(String key, float defaultValue);

    boolean getBoolean(String key);

    boolean getBoolean(String key, boolean defaultValue);

    String getString(String key);

    String getString(String key, String defaultValue);

    Date getDate(String key);

    Date getDate(String key, Date defaultValue);

    Date getDate(String key, String format);

    Date getDate(String key, String format, Date defaultValue);

    JSONObject getJSON(String key);

    JSONArray getJSONArray(String key);

    /**
     *
     * @param key
     * @param object
     * @param <T>
     * @return 上一个对象
     */
    <T> T set(String key, Object object);

    <T> T get(String key);

    Object getValue(Property property,boolean required);
}
