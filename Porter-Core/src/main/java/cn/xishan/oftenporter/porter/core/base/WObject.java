package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.advanced.IExtraEntitySupport;
import cn.xishan.oftenporter.porter.core.advanced.IListenerAdder;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.*;
import cn.xishan.oftenporter.porter.core.sysset.PorterNotInnerSync;
import cn.xishan.oftenporter.porter.core.sysset.PorterSync;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;
import cn.xishan.oftenporter.porter.simple.DefaultListenerAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口中间对象。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public abstract class WObject implements IListenerAdder<WObject.IFinalListener>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WObject.class);

    public interface IFinalListener
    {
        void beforeFinal(WObject wObject) throws Throwable;

        /**
         * 不会调用{@linkplain #afterFinal(WObject)}
         *
         * @param wObject
         * @param throwable
         * @throws Throwable
         */
        void onFinalException(WObject wObject, Throwable throwable) throws Throwable;

        void afterFinal(WObject wObject) throws Throwable;
    }


    /**
     * 类必须参数值数组。
     */
    public Object[] cn;
    /**
     * 类非必需参数值数组。
     */
    public Object[] cu;
    public Object[] cinner;
    /**
     * 类参数名称对象。
     */
    public InNames cInNames;

    /**
     * 函数必须参数值数组。
     */
    public Object[] fn;
    /**
     * 函数非必需参数值数组。
     */
    public Object[] fu;
    public Object[] finner;
    /**
     * 函数参数名称对象。
     */
    public InNames fInNames;

    /**
     * 如果当前是rest，则其值为""或非空;否则为null.
     */
    public String restValue;

    private Map<String, Object> requestDataMap = null;

    protected static final ThreadLocal<WeakReference<WObject>> threadLocal = new ThreadLocal<>();

    public WObject()
    {

    }

    public static WObject fromThreadLocal()
    {
        WeakReference<WObject> reference = threadLocal.get();
        return reference == null ? null : reference.get();
    }

    public abstract WRequest getRequest();

    public abstract WResponse getResponse();

    /**
     * 获取参数源，此参数源包含所有的参数（路径参数优先）。
     *
     * @return
     */
    public abstract ParamSource getParamSource();

    /**
     * 是否是内部请求。
     *
     * @return
     */
    public abstract boolean isInnerRequest();

    public boolean isOriginalRequest()
    {
        return original() == this;
    }

    /**
     * 获取函数上绑定的对象。
     */
    public abstract <T> T fentity(int index);

    /**
     * 获取类上绑定的对象。
     */
    public abstract <T> T centity(int index);

    /**
     * 见{@linkplain IExtraEntitySupport},对于porter函数的形参、会以key和类名进行获取。
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> T extraEntity(String key)
    {
        return null;
    }

    /**
     * 见{@linkplain #savedObject(String) savedObject(Class.getName())}.
     */
    public <T> T savedObject(Class<T> key)
    {
        T obj = savedObject(key.getName());
        return obj;
    }


    /**
     * 获取当前context运行期对象实例。
     * 见{@linkplain PorterConf#addContextAutoSet(String, Object)}
     */
    public abstract <T> T savedObject(String key);


    /**
     * 见{@linkplain #gsavedObject(String) gsavedObject(Class.getName())}.
     */
    public <T> T gsavedObject(Class<T> key)
    {
        T obj = gsavedObject(key.getName());
        return obj;
    }


    /**
     * 获取全局运行期对象.
     * 见{@linkplain CommonMain#addGlobalAutoSet(String, Object)}
     */
    public abstract <T> T gsavedObject(String key);

    public abstract Delivery delivery();

    public abstract UrlDecoder.Result url();

    /**
     * 得到所属的接口框架名称。
     *
     * @return
     */
    public abstract PName getPName();

    public <T> T cnOf(int index)
    {
        T t = (T) cn[index];
        return t;
    }

    public <T> T cuOf(int index)
    {
        T t = (T) cu[index];
        return t;
    }

    public <T> T cuOf(int index, T defaultValue)
    {
        T t = (T) cu[index];
        if (t == null)
        {
            t = defaultValue;
        }
        return t;
    }

    public <T> T fnOf(int index)
    {
        T t = (T) fn[index];
        return t;
    }

    public <T> T fuOf(int index)
    {
        T t = (T) fu[index];
        return t;
    }

    public <T> T fuOf(int index, T defaultValue)
    {
        T t = (T) fu[index];
        if (t == null)
        {
            t = defaultValue;
        }
        return t;
    }

    /**
     * 获取必须参数
     *
     * @param name
     * @param <T>
     * @return
     */
    public <T> T nece(String name)
    {
        T t = getParamSource().getNeceParam(name);
        return t;
    }

    /**
     * 获取非必须参数
     *
     * @param name
     * @param <T>
     * @return
     */
    public <T> T unece(String name)
    {
        T t = getParamSource().getParam(name);
        return t;
    }

    /**
     * 使用当前请求的接口方法。
     *
     * @param funTied
     * @param INameValues
     * @param callback
     */
    public void innerRequest(String funTied, INameValues INameValues, PCallback callback)
    {
        innerRequest(funTied, getRequest().getMethod(), INameValues, callback, true);
    }


//    private static class Temp
//    {
//        OftenCallException exception;
//    }

    /**
     * @param funTied
     * @param method
     * @param INameValues
     * @param callback
     * @param throwWCallException 是否在返回码不为成功时抛出异常。
     */
    public void innerRequest(String funTied, PortMethod method, INameValues INameValues, PCallback callback,
            boolean throwWCallException)
    {
        StringBuilder builder = new StringBuilder();
        UrlDecoder.Result result = url();
        builder.append('/').append(result.contextName()).append('/').append(result.classTied()).append('/');
        builder.append(funTied == null ? "" : funTied);
        PRequest request = PRequest.withNewPath(this, builder.toString(), method, getRequest(), true);
        request.addParamAll(INameValues);
        if (throwWCallException)
        {
            OftenCallException[] oftenCallExceptions = new OftenCallException[1];
            delivery().innerBridge().request(request, lResponse ->
            {
                Object rs = lResponse.getResponse();
                if (rs != null && rs instanceof JResponse)
                {
                    JResponse jResponse = (JResponse) rs;
                    if (jResponse.isNotSuccess())
                    {
                        oftenCallExceptions[0] = new OftenCallException(jResponse);
                        return;
                    }
                }
                if (callback != null)
                {
                    callback.onResponse(lResponse);
                }
            });
            if (oftenCallExceptions[0] != null)
            {
                throw oftenCallExceptions[0];
            }
        } else
        {
            delivery().innerBridge().request(request, callback);
        }
    }

    public void pushClassTied(String classTied)
    {
        url().push(new UrlDecoder.TiedValue(classTied, null));
    }

    public String popClassTied()
    {
        UrlDecoder.TiedValue tiedValue = url().pop();
        return tiedValue == null ? null : tiedValue.classTied;
    }

    public PorterSync newSyncPorter(SyncOption syncOption)
    {
        return syncOption.build(this, true);
    }

    public PorterNotInnerSync newSyncNotInnerPorter(SyncOption syncOption)
    {
        return (PorterNotInnerSync) syncOption.build(this, false);
    }

    public WObject original()
    {
        WRequest request = getRequest();
        WObject originalObject = request == null ? null : request.getOriginalWObject();
        return originalObject == null ? this : originalObject;
    }


    /**
     * 见{@linkplain #putRequestData(String, Object)}
     */
    public <T> T putRequestData(Class<?> clazz, Object value)
    {
        return putRequestData(clazz.getName(), value);
    }


    /**
     * 设置数据， 对整个请求有效。
     *
     * @param name  属性名
     * @param value 属性值
     * @return 返回上一次的值
     */
    public <T> T putRequestData(String name, Object value)
    {
        LOGGER.debug("name={},value={}", name, value);
        WObject wObject = original();
        if (wObject.requestDataMap == null)
        {
            wObject.requestDataMap = new HashMap<>();
        }
        return (T) wObject.requestDataMap.put(name, value);
    }

    /**
     * 见{@linkplain #putCurrentRequestData(String, Object)}
     */
    public <T> T putCurrentRequestData(Class<?> clazz, Object value)
    {
        return putCurrentRequestData(clazz.getName(), value);
    }

    /**
     * 设置数据，只对当前请求有效。
     *
     * @param name  属性名
     * @param value 属性值
     * @return 返回上一次的值
     */
    public <T> T putCurrentRequestData(String name, Object value)
    {
        LOGGER.debug("name={},value={}", name, value);
        if (this.requestDataMap == null)
        {
            this.requestDataMap = new HashMap<>();
        }
        return (T) this.requestDataMap.put(name, value);
    }

    public <T> T getRequestData(Class<T> clazz)
    {
        return getRequestData(clazz.getName());
    }

    public <T> T getRequestData(String name)
    {
        WObject wObject = original();
        if (wObject.requestDataMap == null)
        {
            return null;
        }
        return (T) wObject.requestDataMap.get(name);
    }

    public <T> T getCurrentRequestData(Class<T> clazz)
    {
        return getCurrentRequestData(clazz.getName());
    }

    public <T> T getCurrentRequestData(String name)
    {
        if (this.requestDataMap == null)
        {
            return null;
        }
        return (T) this.requestDataMap.get(name);
    }

    public <T> T removeRequestData(String name)
    {
        WObject wObject = original();
        if (wObject.requestDataMap == null)
        {
            return null;
        }
        return (T) wObject.requestDataMap.remove(name);
    }

    private IListenerAdder<IFinalListener> listenerAdder;

    private IListenerAdder<IFinalListener> getListenerAdder(boolean willCreate)
    {
        WObject wObject = original();
        if (wObject.listenerAdder == null && willCreate)
        {
            wObject.listenerAdder = new DefaultListenerAdder<>();
        }
        return wObject.listenerAdder;
    }

    @Override
    public String addListener(IFinalListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException();
        }
        return getListenerAdder(true).addListener(listener);
    }


    @Override
    public void addListener(String name, IFinalListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException();
        }
        getListenerAdder(true).addListener(name, listener);
    }

    @Override
    public IFinalListener removeListener(String name)
    {
        IListenerAdder<IFinalListener> listenerIListenerAdder = getListenerAdder(false);
        return listenerIListenerAdder == null ? null : listenerIListenerAdder.removeListener(name);
    }

    @Override
    public Enumeration<IFinalListener> listeners(int order)
    {
        IListenerAdder<IFinalListener> listenerIListenerAdder = getListenerAdder(false);
        return listenerIListenerAdder == null ? EnumerationImpl.getEMPTY() : listenerIListenerAdder.listeners(order);
    }
}
