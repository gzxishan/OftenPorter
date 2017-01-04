package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortOut;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public final class PorterOfFun
{
    Method method;
    //函数的形参个数。
    int argCount;
    _PortOut portOut;
    _PortIn portIn;
    InObj inObj;

    public _PortOut getPortOut()
    {
        return portOut;
    }

    public InObj getInObj()
    {
        return inObj;
    }

    public Method getMethod()
    {
        return method;
    }

    public _PortIn getPortIn()
    {
        return portIn;
    }

    /**
     * 函数的形参列表数。
     *
     * @return
     */
    public int getArgCount()
    {
        return argCount;
    }
}
