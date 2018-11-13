package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.advanced.IExtraEntitySupport;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortIn;
import cn.xishan.oftenporter.porter.core.annotation.deal._PortOut;
import cn.xishan.oftenporter.porter.core.advanced.IArgumentsFactory;
import cn.xishan.oftenporter.porter.core.advanced.IArgumentsFactory.IArgsHandle;
import cn.xishan.oftenporter.porter.core.base.OftenContextInfo;
import cn.xishan.oftenporter.porter.core.base.PortFunType;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public abstract class PorterOfFun extends IExtraEntitySupport.ExtraEntitySupportImpl implements ObjectGetter
{

    private static final Logger LOGGER = LoggerFactory.getLogger(PorterOfFun.class);

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
    _PortOut portOut;
    _PortIn portIn;
    OftenEntities oftenEntities;
    Porter porter;
    private IArgsHandle argsHandle;


    private AspectOperationOfPortIn.Handle[] handles;

    public IArgsHandle getArgsHandle()
    {
        return argsHandle;
    }

    public void setArgsHandle(IArgsHandle argsHandle)
    {
        LOGGER.debug("{}:{}", this, argsHandle);
        this.argsHandle = argsHandle;
    }

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
     * 最终的args由{@linkplain IArgsHandle}确定,另见{@linkplain PorterConf#setArgumentsFactory(IArgumentsFactory)},
     * {@linkplain #putInvokeArg(OftenObject, String, Object)}.
     *
     * @param oftenObject
     * @param args
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public final Object invokeByHandleArgs(OftenObject oftenObject, Object... args) throws Exception
    {
        Method javaMethod = getMethod();
        IArgsHandle argsHandle = this.argsHandle;
        LOGGER.debug("{}:{}", this, argsHandle);
        Object[] finalArgs = argsHandle.getInvokeArgs(oftenObject, this, javaMethod, args);
        return javaMethod.invoke(getObject(), finalArgs);
    }

    public ArgData getArgData(OftenObject oftenObject)
    {
        ArgData argData = oftenObject.getCurrentRequestData(ArgData.class);
        return argData;
    }

    public void putInvokeArg(OftenObject oftenObject, String argName, Object value)
    {
        ArgData argData = oftenObject.getCurrentRequestData(ArgData.class);
        if (argData == null)
        {
            argData = new ArgData();
            oftenObject.putCurrentRequestData(ArgData.class, argData);
        }
        argData.map.put(argName, value);
    }

    public void putInvokeArg(OftenObject oftenObject, Class argType, Object value)
    {
        this.putInvokeArg(oftenObject, argType.getName(), value);
    }

    public void putInvokeArg(OftenObject oftenObject, Object value)
    {
        this.putInvokeArg(oftenObject, value.getClass(), value);
    }

    public boolean hasParameterType(OftenObject oftenObject, Class type)
    {
        IArgsHandle argsHandle = this.argsHandle;
        return argsHandle.hasParameterType(oftenObject, this, getMethod(), type);
    }

    public _PortOut getPortOut()
    {
        return portOut;
    }

    public OftenEntities getOftenEntities()
    {
        return oftenEntities;
    }

    void initEntities(Map<String, One> extraEntityMap, SthDeal sthDeal,
            InnerContextBridge innerContextBridge, AutoSetHandle autoSetHandle) throws Exception
    {
        porter.initOftenEntitiesHandle(getOftenEntities());
        initAndGetExtraEntities(extraEntityMap, sthDeal, innerContextBridge, autoSetHandle);
    }

    private void initAndGetExtraEntities(Map<String, One> extraEntityMap, SthDeal sthDeal,
            InnerContextBridge innerContextBridge, AutoSetHandle autoSetHandle) throws Exception
    {
        for (String key : getExtraKeySet())
        {
            One one = sthDeal.dealOPEntity(getExtraEntity(key), method, innerContextBridge, autoSetHandle);
            porter.initOftenEntitiesHandle(one);
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


//    @Override
//    public String toString()
//    {
//        return method.toString();
//    }

    public void startHandles(OftenObject oftenObject)
    {
        if (handles != null)
        {
            for (AspectOperationOfPortIn.Handle handle : handles)
            {
                handle.onStart(oftenObject);
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

    public String getPath()
    {
        Porter porter = getFinalPorter();
        OftenContextInfo contextInfo = porter.getContextInfo();
        String path = "/" + contextInfo.getContextName() + "/" + porter.getPortIn()
                .getTiedNames()[0] + "/" + getMethodPortIn()
                .getTiedNames()[0];
        return path;
    }
}
