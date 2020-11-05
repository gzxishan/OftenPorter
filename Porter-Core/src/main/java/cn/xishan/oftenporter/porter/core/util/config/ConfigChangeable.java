package cn.xishan.oftenporter.porter.core.util.config;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @author Created by https://github.com/CLovinr on 2020-11-04.
 */
public class ConfigChangeable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigChangeable.class);

    private List<OnConfigDataChange> configDataChanges = new Vector<>();
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

    public void removeOnConfigValueChange(OnConfigValueChange listener)
    {
        onConfigDataChangeForValue.remove(listener);
    }

    public <T> ChangeableProperty<T> getConfigValueProperty(Class<T> type, String attr, T currentValue)
    {
        return getConfigValueProperty(type, attr, currentValue, null);
    }

    public <T> ChangeableProperty<T> getConfigValueProperty(Class<T> type, String attr, T currentValue, T defaultValue)
    {
        ChangeablePropertyWrapper<T> wrapper = new ChangeablePropertyWrapper<T>(attr,currentValue, defaultValue)
        {
            @Override
            public void release()
            {
                super.release();
                removeOnConfigValueChange(this);
            }

            @Override
            protected void onChangeFinished(T newValue, T oldValue)
            {
                //通知其他监听器
                onConfigDataChangeForValue.changeForChangeableProperty(attr, newValue, oldValue);
            }
        };
        onConfigDataChangeForValue.add(type, wrapper, new String[]{attr});
        return wrapper;
    }


    public void addOnConfigDataChangeListener(OnConfigDataChange onConfigChange)
    {
        configDataChanges.add(onConfigChange);
    }

    public void removeOnConfigDataChangeListener(OnConfigDataChange listener)
    {
        Iterator iterator = configDataChanges.iterator();
        while (iterator.hasNext())
        {
            Object object = iterator.next();
            if (object == listener)
            {
                iterator.remove();
            }
        }
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
