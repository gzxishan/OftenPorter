package cn.xishan.oftenporter.porter.simple;

import cn.xishan.oftenporter.porter.core.advanced.IListenerAdder;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public class DefaultListenerAdder<T> implements IListenerAdder<T>
{
    private Map<String, T> listenerMap;

    public DefaultListenerAdder()
    {
        this.listenerMap = new LinkedHashMap<>();
    }

    @Override
    public synchronized void add(String name, T listener)
    {
        listenerMap.put(name, listener);
    }

    @Override
    public synchronized T remove(String name)
    {
        return listenerMap.remove(name);
    }

    @Override
    public synchronized Enumeration<T> listeners()
    {
        EnumerationImpl<T> enumeration = new EnumerationImpl<>(
                listenerMap.values().iterator());
        return enumeration;
    }
}
