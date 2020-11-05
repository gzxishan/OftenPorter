package cn.xishan.oftenporter.porter.core.util.config;

/**
 * @author Created by https://github.com/CLovinr on 2020-11-04.
 */
class ChangeablePropertyWrapper<T> extends ChangeableProperty<T> implements OnConfigValueChange<T>
{

    public ChangeablePropertyWrapper(String attr, T value, T defaultValue)
    {
        super(attr, value, defaultValue);
    }

    @Override
    public void onChange(String attr, T newValue, T oldValue) throws Exception
    {
        this.submitValue(newValue);
    }

}
