package cn.xishan.oftenporter.porter.core.util.config;

import cn.xishan.oftenporter.porter.core.util.OftenTool;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * 用于处理json配置的单个属性变化。
 *
 * @author Created by https://github.com/CLovinr on 2020-11-03.
 */
class OnConfigDataChangeForValue implements OnConfigDataChange
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OnConfigDataChangeForValue.class);

    static class Wrapper
    {
        final Class type;
        final OnConfigValueChange change;
        final String[] attrs;

        public Wrapper(Class type, OnConfigValueChange change, String[] attrs)
        {
            this.type = type;
            this.change = change;
            this.attrs = attrs;
        }
    }

    private List<Wrapper> wrappers = new Vector<>();

    public void add(Class type, OnConfigValueChange change, String[] attrs)
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
            wrappers.add(new Wrapper(type, change, attrs));
        }
    }

    public void remove(OnConfigValueChange change)
    {
        Iterator<Wrapper> iterator = wrappers.iterator();
        while (iterator.hasNext())
        {
            Wrapper wrapper = iterator.next();
            if (wrapper.change == change)
            {
                iterator.remove();
            }
        }
    }

    @Override
    public void onChange(JSONObject newConfig, JSONObject oldConfig)
    {
        for (Wrapper wrapper : wrappers)
        {
            for (String attr : wrapper.attrs)
            {
                try
                {
                    Object oldValue = OftenTool.getObjectAttr(wrapper.type, oldConfig, attr, null);
                    Object newValue = OftenTool.getObjectAttr(wrapper.type, newConfig, attr, null);
                    if (!OftenTool.isEqual(newValue, oldValue))
                    {
                        wrapper.change.onChange(attr, newValue, oldValue, newConfig, oldConfig);
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
