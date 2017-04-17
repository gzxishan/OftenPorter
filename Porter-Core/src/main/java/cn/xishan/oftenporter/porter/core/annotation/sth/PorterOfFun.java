package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.deal._PortAfter;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortBefore;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortOut;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public abstract class PorterOfFun implements ObjectGetter
{
    Method method;
    //函数的形参个数。
    int argCount;
    _PortOut portOut;
    _PortIn portIn;
    InObj inObj;
    Porter porter;


    _PortBefore[] portBefores;
    _PortAfter[] portAfters;

    public _PortBefore[] getPortBefores()
    {
        return portBefores;
    }

    public _PortAfter[] getPortAfters()
    {
        return portAfters;
    }

   public static PorterOfFun withMethodAndObject(Method method, ObjectGetter objectGetter)
    {
        PorterOfFun porterOfFun = new PorterOfFun()
        {
            @Override
            public Object getObject()
            {
                return objectGetter.getObject();
            }
        };
        porterOfFun.method = method;
        return porterOfFun;
    }

    public Porter getPorter()
    {
        return porter;
    }

    @Override
    public abstract Object getObject();

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

    public _PortIn getMethodPortIn()
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
