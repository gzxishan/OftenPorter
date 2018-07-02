package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.IListenerAdder;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;

import java.util.*;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class DefaultListenerAdder<T> implements IListenerAdder<T>
{

    private LinkedHashMap<String, T> listeners = new LinkedHashMap<>(5);

    public DefaultListenerAdder()
    {
    }

    @Override
    public synchronized void addListener(String name, T listener)
    {
        listeners.put(name, listener);
    }

    @Override
    public String addListener(T listener)
    {
        String name = KeyUtil.randomUUID();
        addListener(name, listener);
        return name;
    }

    @Override
    public synchronized T removeListener(String name)
    {
        T t = listeners.remove(name);
        return t;
    }

    @Override
    public synchronized Enumeration<T> listeners(int order)
    {
        T[] ts = (T[]) listeners.values().toArray();
        Enumeration<T> e = EnumerationImpl.fromArray(ts, order >= 0);
        return e;
    }
}
