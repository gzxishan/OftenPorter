package cn.xishan.oftenporter.porter.core.init;

import cn.xishan.oftenporter.porter.core.base.ListenerAdder;
import cn.xishan.oftenporter.porter.core.base.OnPorterAddListener;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/3/18.
 */
class ListenerAdderImpl implements ListenerAdder<OnPorterAddListener>
{
    private Map<String, OnPorterAddListener> listenerMap;

    public ListenerAdderImpl()
    {
        this.listenerMap = new LinkedHashMap<>();
    }

    @Override
    public synchronized void add(String name, OnPorterAddListener listener)
    {
        listenerMap.put(name, listener);
    }

    @Override
    public synchronized OnPorterAddListener remove(String name)
    {
        return listenerMap.remove(name);
    }

    @Override
    public synchronized Enumeration<OnPorterAddListener> listeners()
    {
        EnumerationImpl<OnPorterAddListener> enumeration = new EnumerationImpl<>(
                listenerMap.values().iterator());
        return enumeration;
    }
}
