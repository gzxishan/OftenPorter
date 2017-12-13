package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortOut;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.lang.reflect.InvocationTargetException;
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
     * 返回null表示没有切面处理器。
     *
     * @return
     */
    public AspectFunOperation.Handle[] getHandles()
    {
        return handles;
    }

    public void setHandles(AspectFunOperation.Handle[] handles)
    {
        this.handles = handles;
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
     *
     * @return
     */
    @Override
    public abstract Object getObject();

    /**
     * @param wObject
     * @param optionArgs 可选参数,为null采用默认的处理。
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object invoke(WObject wObject, Object[] optionArgs) throws InvocationTargetException, IllegalAccessException
    {
        Method javaMethod = getMethod();
        if (optionArgs != null)
        {
            return javaMethod.invoke(getObject(), optionArgs);
        } else if (getArgCount() == 0)
        {
            return javaMethod.invoke(getObject());
        } else
        {
            return javaMethod.invoke(getObject(), wObject);
        }
    }

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

    public void startHandles(WObject wObject)
    {
        if (handles != null)
        {
            for (AspectFunOperation.Handle handle : handles)
            {
                handle.onStart(wObject);
            }
        }
    }

    public void destroyHandles()
    {
        if (handles != null)
        {
            for (AspectFunOperation.Handle handle : handles)
            {
                handle.onDestroy();
            }
        }
    }
}
