package cn.xishan.oftenporter.porter.core.util.config;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Created by https://github.com/CLovinr on 2020-11-04.
 */
public class ConfigChangeable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigChangeable.class);

    private Set<OnConfigDataChange> configDataChanges = new LinkedHashSet<>();
    private OnConfigDataChangeForValue onConfigDataChangeForValue;

    public ConfigChangeable()
    {
        onConfigDataChangeForValue = new OnConfigDataChangeForValue();
        addOnConfigDataChangeListener(onConfigDataChangeForValue);
    }

    /**
     * 监听具体属性变化
     *
     * @param type   属性的数据类型
     * @param change 监听器
     * @param attrs  属性格式为：attr1.attr2,attrA.attrB.attrC等
     * @param <T>
     */
    public <T> void addOnConfigValueChange(Class<T> type, OnConfigValueChange<T> change, String... attrs)
    {
        onConfigDataChangeForValue.add(type, change, attrs);
    }

    public void removeOnConfigValueChange(OnConfigValueChange change)
    {
        onConfigDataChangeForValue.remove(change);
    }

    public void addOnConfigDataChangeListener(OnConfigDataChange onConfigChange)
    {
        configDataChanges.add(onConfigChange);
    }

    public void removeOnConfigDataChangeListener(OnConfigDataChange onConfigChange)
    {
        configDataChanges.remove(onConfigChange);
    }

    public void submitChange(JSONObject newConfig, JSONObject oldConfig)
    {
        for (OnConfigDataChange configChange : configDataChanges)
        {
            try
            {
                configChange.onChange(newConfig, oldConfig);
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }
}
