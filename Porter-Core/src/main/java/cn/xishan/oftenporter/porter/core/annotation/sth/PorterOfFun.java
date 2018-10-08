package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.advanced.IExtraEntitySupport;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortOut;
import cn.xishan.oftenporter.porter.core.advanced.IArgumentsFactory;
import cn.xishan.oftenporter.porter.core.advanced.IArgumentsFactory.IArgsHandle;
import cn.xishan.oftenporter.porter.core.base.PortFunType;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.init.PorterConf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public abstract class PorterOfFun extends IExtraEntitySupport.ExtraEntitySupportImpl implements ObjectGetter
{

    public static class ArgData
    {
        private Map<String, Object> map;

        public ArgData()
        {
            map = new HashMap<>();
        }

        public Map<String, Object> getDataMap()
        {
            return map;
        }

        public Object getArg(String argType)
        {
            return map.get(argType);
        }
    }

    Method method;
    //函数的形参个数。
    int argCount;
    _PortOut portOut;
    _PortIn portIn;
    OPEntities opEntities;
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
     * 最终的args由{@linkplain IArgsHandle}确定,另见{@linkplain PorterConf#setArgumentsFactory(IArgumentsFactory)},
     * {@linkplain #putInvokeArg(WObject, String, Object)}.
     *
     * @param wObject
     * @param args
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public final Object invokeByHandleArgs(WObject wObject, Object... args) throws Exception
    {
        Method javaMethod = getMethod();
        IArgumentsFactory argumentsFactory = porter.getArgumentsFactory();
        IArgsHandle argsHandle = argumentsFactory.getArgsHandle(this);
        Object[] finalArgs = argsHandle.getInvokeArgs(wObject, this, javaMethod, args);
        return javaMethod.invoke(getObject(), finalArgs);
    }

    public ArgData getArgData(WObject wObject)
    {
        ArgData argData = wObject.getCurrentRequestData(ArgData.class);
        return argData;
    }

    public void putInvokeArg(WObject wObject, String argName, Object value)
    {
        ArgData argData = wObject.getCurrentRequestData(ArgData.class);
        if (argData == null)
        {
            argData = new ArgData();
            wObject.putCurrentRequestData(ArgData.class, argData);
        }
        argData.map.put(argName, value);
    }

    public void putInvokeArg(WObject wObject, Class argType, Object value)
    {
        this.putInvokeArg(wObject, argType.getName(), value);
    }

    public void putInvokeArg(WObject wObject, Object value)
    {
        this.putInvokeArg(wObject, value.getClass(), value);
    }

    public boolean hasParameterType(WObject wObject, Class type)
    {
        IArgumentsFactory argumentsFactory = porter.getArgumentsFactory();
        IArgsHandle argsHandle = argumentsFactory.getArgsHandle(this);
        return argsHandle.hasParameterType(wObject, this, getMethod(), type);
    }

    public _PortOut getPortOut()
    {
        return portOut;
    }

    public OPEntities getOPEntities()
    {
        return opEntities;
    }

    void initEntities(Map<String, One> extraEntityMap, SthDeal sthDeal,
            InnerContextBridge innerContextBridge, AutoSetHandle autoSetHandle) throws Exception
    {
        porter.initOPEntitiesHandle(getOPEntities());
        initAndGetExtraEntities(extraEntityMap, sthDeal, innerContextBridge, autoSetHandle);
    }

    private void initAndGetExtraEntities(Map<String, One> extraEntityMap, SthDeal sthDeal,
            InnerContextBridge innerContextBridge, AutoSetHandle autoSetHandle) throws Exception
    {
        for (String key : getExtraKeySet())
        {
            One one = sthDeal.dealOPEntity(getExtraEntity(key), method, innerContextBridge, autoSetHandle);
            porter.initOPEntitiesHandle(one);
            extraEntityMap.put(key, one);
        }
        clearExtra();
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
