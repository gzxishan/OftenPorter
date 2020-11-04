package cn.xishan.oftenporter.porter.core.util.config;

import com.alibaba.fastjson.JSONObject;

/**
 * 监听某个属性变化。
 *
 * @author Created by https://github.com/CLovinr on 2020-11-03.
 */
public interface OnConfigValueChange<T>
{
    default void onChange(String attr, T newValue, T oldValue, JSONObject newJson, JSONObject oldJson) throws Exception
    {
        onChange(attr, newValue, oldValue);
    }

    void onChange(String attr, T newValue, T oldValue) throws Exception;

}
