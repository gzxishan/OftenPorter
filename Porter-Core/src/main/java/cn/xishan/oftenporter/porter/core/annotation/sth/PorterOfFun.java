package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortOut;
import cn.xishan.oftenporter.porter.core.base.IArgumentsFactory;
import cn.xishan.oftenporter.porter.core.base.IArgumentsFactory.IArgsHandle;
import cn.xishan.oftenporter.porter.core.base.PortFunType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.init.PorterConf;

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
    OPEntities OPEntities;
    Porter porter;

    private AspectOperationOfPortIn.Handle[] handles;


    public PorterOfFun(Method method)
    {
        this.method = method;
    }

    public final boolean isInner()
    {
        PortFunType portFunType = portIn.getPortFunType();
        return portFunType == PortFunType.INNER || portFunType == PortFunType.FAST_INNER;
    }

    public final boolean isFastInner()
    {
        PortFunType portFunType = portIn.getPortFunType();
        return portFunType == PortFunType.FAST_INNER;
    }

    /**
     * 返回null表示没有切面处理器。
     *
     * @return
     */
    public AspectOperationOfPortIn.Handle[] getHandles()
    {
        return handles;
    }

    public void setHandles(AspectOperationOfPortIn.Handle[] handles)
    {
        this.handles = handles;
    }


    public static PorterOfFun withMethodAndObject(Method method, ObjectGetter objectGetter)
    {
        PorterOfFun porterOfFun = new PorterOfFun(method)
        {
            @Override
            public Object getObject()
            {
                return objectGetter.getObject();
            }
        };
        return porterOfFun;
    }

    public Porter getPorter()
    {
        return porter;
    }

    /**
     * 见{@linkplain Porter#getFinalPorterObject()}
     *
     * @return
     */
    public Object getFinalPorterObject()
    {
        return porter.getFinalPorterObject();
    }


    /**
     * 见{@linkplain Porter#getFinalPorter()}
     *
     * @return
     */
    public Porter getFinalPorter()
    {
        return porter.getFinalPorter();
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
    @Deprecated
    public final Object invoke(WObject wObject,
            Object[] optionArgs) throws InvocationTargetException, IllegalAccessException
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


    /**
     * 调用函数
     *
     * @param args 参数列表
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public final Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException
    {
        Method javaMethod = getMethod();
        return javaMethod.invoke(getObject(), args);
    }


    /**
     * 函数的参数列表为()或(WObject)
     *
     * @param wObject
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public final Object invoke(WObject wObject) throws InvocationTargetException, IllegalAccessException
    {
        Method javaMethod = getMethod();
        if (getArgCount() == 0)
        {
            return javaMethod.invoke(getObject());
        } else
        {
            return javaMethod.invoke(getObject(), wObject);
        }
    }

    /**
     * 最终的args由{@linkplain IArgsHandle}确定,另见{@linkplain PorterConf#setArgumentsFactory(IArgumentsFactory)}.
     *
     * @param wObject
     * @param args
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public final Object invokeByHandleArgs(WObject wObject,
            Object... args) throws Exception
    {
        Method javaMethod = getMethod();
        IArgumentsFactory argumentsFactory = porter.getArgumentsFactory();
        IArgsHandle argsHandle = argumentsFactory.getArgsHandle(this);
        Object[] finalArgs = argsHandle.getInvokeArgs(wObject, javaMethod, args);
        return javaMethod.invoke(getObject(), finalArgs);
    }

    public boolean hasParameterType(WObject wObject, Class type)
    {
        IArgumentsFactory argumentsFactory = porter.getArgumentsFactory();
        IArgsHandle argsHandle = argumentsFactory.getArgsHandle(this);
        return argsHandle.hasParameterType(wObject, getMethod(), type);
    }

    public _PortOut getPortOut()
    {
        return portOut;
    }

    public OPEntities getInObj()
    {
        return OPEntities;
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
    public final int getArgCount()
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

    @Override
    public String toString()
    {
        return method.toString();
    }

    public void startHandles(WObject wObject)
    {
        if (handles != null)
        {
            for (AspectOperationOfPortIn.Handle handle : handles)
            {
                handle.onStart(wObject);
            }
        }
    }

    public void destroyHandles()
    {
        if (handles != null)
        {
            for (AspectOperationOfPortIn.Handle handle : handles)
            {
                handle.onDestroy();
            }
        }
    }
}
