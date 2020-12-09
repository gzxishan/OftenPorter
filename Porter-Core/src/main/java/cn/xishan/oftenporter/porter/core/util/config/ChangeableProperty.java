package cn.xishan.oftenporter.porter.core.util.config;

import cn.xishan.oftenporter.porter.core.util.OftenTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Vector;

/**
 * @author Created by https://github.com/CLovinr on 2020-11-04.
 */
public class ChangeableProperty<T> implements AutoCloseable, OnPropertyChange<T>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeableProperty.class);


    protected T value;
    protected T defaultValue;
    private List<OnPropertyChange<T>> changeList = new Vector<>();
    private String attr;

    public ChangeableProperty(T value)
    {
        this(null, value);
    }

    public ChangeableProperty(String attr, T value)
    {
        this.attr = attr;
        this.value = value;
    }

    public ChangeableProperty(String attr, T value, T defaultValue)
    {
        this.attr = attr;
        this.value = value == null ? defaultValue : value;
        this.defaultValue = defaultValue;
    }

    public String getAttr()
    {
        return attr;
    }

    public void setAttr(String attr)
    {
        this.attr = attr;
    }

    public T getValue()
    {
        return value;
    }

    public ChangeableProperty<T> addListener(OnPropertyChange<T> change)
    {
        return addListener(false, change);
    }

    public ChangeableProperty<T> addListener(boolean trigger, OnPropertyChange<T> change)
    {
        changeList.add(change);
        if (trigger)
        {
            change.onChange(this, getAttr(), getValue(), null);
        }
        return this;
    }

    public ChangeableProperty<T> removeListener(OnPropertyChange<T> change)
    {
        changeList.remove(change);
        return this;
    }


    protected void onChangeFinished(T newValue, T oldValue)
    {

    }

    public ChangeableProperty<T> submitValue(T newValue)
    {
        if (newValue == null)
        {
            newValue = defaultValue;
        }

        T oldValue = getValue();
        if (OftenTool.notEqual(newValue, oldValue))
        {
            this.value = newValue;
            for (OnPropertyChange<T> change : changeList)
            {
                if (change != this)
                {
                    try
                    {
                        change.onChange(this, attr, newValue, oldValue);
                    } catch (Exception e)
                    {
                        LOGGER.warn(e.getMessage(), e);
                    }
                }
            }
            onChangeFinished(newValue, oldValue);
        }
        return this;
    }

    /**
     * 释放该属性。
     */
    public void release()
    {
        value = null;
        defaultValue = null;
        changeList.clear();
    }

    @Override
    public void close()
    {
        release();
    }

    @Override
    public void onChange(ChangeableProperty<T> property, String attr, T newValue, T oldValue)
    {
        this.submitValue(newValue);
    }
}
