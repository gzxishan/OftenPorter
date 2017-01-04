package cn.xishan.oftenporter.porter.core.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class EnumerationImpl<E> implements Enumeration<E>
{
    private Iterator<E> iterator;

    public EnumerationImpl(Iterator<E> iterator)
    {
        this.iterator = iterator;
    }

    public EnumerationImpl(Set<E> set)
    {
        this(set.iterator());
    }

    @Override
    public boolean hasMoreElements()
    {
        return iterator.hasNext();
    }

    @Override
    public E nextElement()
    {
        return iterator.next();
    }
}
