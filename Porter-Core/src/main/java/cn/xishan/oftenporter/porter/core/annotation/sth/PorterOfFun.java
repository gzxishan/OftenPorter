package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortFilterOne;
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

    AspectFunOperation.Handle[] handles;

    /**
     * 第一个先调用
     */
    _PortFilterOne[] portBefores;
    /**
     * 第一个先调用
     */
    _PortFilterOne[] portAfters;

    public AspectFunOperation.Handle[] getHandles()
    {
        return handles;
    }

    public void setHandles(AspectFunOperation.Handle[] handles)
    {
        this.handles = handles;
    }

    public _PortFilterOne[] getPortBefores()
    {
        return portBefores;
    }

    public _PortFilterOne[] getPortAfters()
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

    /**
     * @return
     * @see Porter#getFinalPorterObject()
     */
    public Object getFinalPorterObject()
    {
        return porter.getFinalPorterObject();
    }

    /**
     * 得到函数所在的对象实例。
     * @return
     */
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

    @Override
    public int hashCode()
    {
        return method.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof PorterOfFun))
        {
            return false;
        }
        PorterOfFun fun = (PorterOfFun) obj;
        return method.equals(fun.method);
    }
}
