package cn.xishan.oftenporter.porter.core.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by https://github.com/CLovinr on 2016/9/2.
 */
public class EnumerationImpl<E> implements Enumeration<E> {

    private static final Enumeration EMPTY = new Enumeration() {
        @Override
        public boolean hasMoreElements() {
            return false;
        }

        @Override
        public Object nextElement() {
            return null;
        }
    };

    public static <E> Enumeration<E> getEMPTY() {
        return EMPTY;
    }

    private Iterator<E> iterator;

    public EnumerationImpl(Iterator<E> iterator) {
        this.iterator = iterator;
    }

    public EnumerationImpl(Set<E> set) {
        this(set.iterator());
    }

    @Override
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    @Override
    public E nextElement() {
        return iterator.next();
    }
}
