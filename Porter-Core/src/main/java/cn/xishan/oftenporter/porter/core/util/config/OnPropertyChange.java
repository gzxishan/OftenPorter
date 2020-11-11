package cn.xishan.oftenporter.porter.core.util.config;

/**
 * @author Created by https://github.com/CLovinr on 2020-11-11.
 */
public interface OnPropertyChange<T>
{
    void onChange(ChangeableProperty<T> property, String attr, T newValue, T oldValue);
}
