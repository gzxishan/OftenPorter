package cn.xishan.oftenporter.porter.core.base;

import java.util.Enumeration;

/**
 * @author Created by https://github.com/CLovinr on 2017/3/18.
 */
public interface ListenerAdder<T>
{
    void add(String name,T listener);
    T remove(String name);
    Enumeration<T> listeners();
}
