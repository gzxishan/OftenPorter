package cn.xishan.oftenporter.porter.core.util.config;

import cn.xishan.oftenporter.porter.core.util.OftenTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Vector;

/**
 * @author Created by https://github.com/CLovinr on 2020-11-04.
 */
public class ChangeableProperty<T>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeableProperty.class);

    public interface OnChange<T>
    {
        void onChange(ChangeableProperty<T> property, T newValue, T oldValue);
    }

    private T value;
    private List<OnChange<T>> changeList = new Vector<>();

    public ChangeableProperty(T value)
    {
        this.value = value;
    }

    public T getValue()
    {
        return value;
    }

    public void addListener(OnChange<T> change)
    {
        changeList.add(change);
    }

    public void removeListener(OnChange<T> change)
    {
        changeList.remove(change);
    }


    public void submitChange(T newValue)
    {
        T value = getValue();
        if (!OftenTool.isEqual(newValue, value))
        {
            for (OnChange<T> change : changeList)
            {
                try
                {
                    change.onChange(this, newValue, value);
                } catch (Exception e)
                {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        }
    }

}
