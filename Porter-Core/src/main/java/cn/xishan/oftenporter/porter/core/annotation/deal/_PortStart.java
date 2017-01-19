package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class _PortStart implements Comparable<_PortStart>
{
    int order;
    PorterOfFun porterOfFun;


    public PorterOfFun getPorterOfFun()
    {
        return porterOfFun;
    }

    public int getOrder()
    {
        return order;
    }

    @Override
    public int compareTo(_PortStart other)
    {
        return order-other.order;
    }
}
