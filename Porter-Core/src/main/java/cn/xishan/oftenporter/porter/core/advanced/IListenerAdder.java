package cn.xishan.oftenporter.porter.core.advanced;

import java.util.Enumeration;

/**
 * @author Created by https://github.com/CLovinr on 2017/3/18.
 */
public interface IListenerAdder<T>
{
    void add(String name,T listener);
    T remove(String name);
    Enumeration<T> listeners();
}
