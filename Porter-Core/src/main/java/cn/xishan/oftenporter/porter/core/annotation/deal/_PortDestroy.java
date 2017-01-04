package cn.xishan.oftenporter.porter.core.annotation.deal;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class _PortDestroy implements Comparable<_PortStart>
{
    int order;
    Method method;

    public Method getMethod()
    {
        return method;
    }

    public int getOrder()
    {
        return order;
    }

    @Override
    public int compareTo(_PortStart other)
    {
        return order - other.order;
    }
}

