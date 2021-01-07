package cn.xishan.oftenporter.porter.core.base;

import cn.xishan.oftenporter.porter.core.JResponse;
import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.advanced.IExtraEntitySupport;
import cn.xishan.oftenporter.porter.core.advanced.IListenerAdder;
import cn.xishan.oftenporter.porter.core.advanced.UrlDecoder;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.annotation.sth.SyncPorterThrowsImpl;
import cn.xishan.oftenporter.porter.core.bridge.BridgeCallback;
import cn.xishan.oftenporter.porter.core.bridge.BridgeName;
import cn.xishan.oftenporter.porter.core.bridge.BridgeRequest;
import cn.xishan.oftenporter.porter.core.bridge.Delivery;
import cn.xishan.oftenporter.porter.core.exception.OftenCallException;
import cn.xishan.oftenporter.porter.core.sysset.IAutoVarGetter;
import cn.xishan.oftenporter.porter.core.sysset.PorterNotInnerSync;
import cn.xishan.oftenporter.porter.core.sysset.PorterSync;
import cn.xishan.oftenporter.porter.core.util.EnumerationImpl;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.core.util.PolyfillUtils;
import cn.xishan.oftenporter.porter.simple.DefaultListenerAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 接口中间对象。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
public abstract class OftenObject implements IListenerAdder<OftenObject.IFinalListener>, IAutoVarGetter
{
    public static final OftenObject EMPTY = new OftenObjectEmpty();
    private static final Logger LOGGER = LoggerFactory.getLogger(OftenObject.class);

    public interface IFinalListener
    {
        void beforeFinal(OftenObject oftenObject) throws Throwable;

        /**
         * 出现异常时，不会调用{@linkplain #afterFinal(OftenObject)}
         *
         * @param oftenObject
         * @param throwable
         * @throws Throwable
         */
        void onFinalException(OftenObject oftenObject, Throwable throwable) throws Throwable;

        void afterFinal(OftenObject oftenObject) throws Throwable;
    }


    /**
     * 类必须参数值数组。
     */
    public Object[] _cn = OftenTool.EMPTY_OBJECT_ARRAY;
    /**
     * 类非必需参数值数组。
     */
    public Object[] _cu = OftenTool.EMPTY_OBJECT_ARRAY;
    public Object[] _cinner = OftenTool.EMPTY_OBJECT_ARRAY;
    /**
     * 类参数名称对象。
     */
    public InNames _cInNames = InNames.EMPTY;

    /**
     * 函数必须参数值数组。
     */
    public Object[] _fn = OftenTool.EMPTY_OBJECT_ARRAY;
    /**
     * 函数非必需参数值数组。
     */
    public Object[] _fu = OftenTool.EMPTY_OBJECT_ARRAY;
    public Object[] _finner = OftenTool.EMPTY_OBJECT_ARRAY;
    /**
     * 函数参数名称对象。
     */
    public InNames _fInNames = InNames.EMPTY;

    private Map<String, Object> requestDataMap = null;

    protected static final ThreadLocal<Stack<WeakReference<OftenObject>>> threadLocal = PolyfillUtils
            .ThreadLocal_withInitial(Stack::new);
    private static final ThreadLocal<Map<String, Object>> threadLocalOfRequestData = PolyfillUtils
            .ThreadLocal_withInitial(HashMap::new);

    public OftenObject()
    {

    }

    public static OftenObject current()
    {
        Stack<WeakReference<OftenObject>> stack = threadLocal.get();
        if (stack.isEmpty())
        {
            return null;
        }
        WeakReference<OftenObject> reference = stack.peek();
        return reference == null ? null : reference.get();
    }

    /**
     * 注意，如果缓存的当前实例，记得每次执行接口（如{@linkplain PorterOfFun#invokeByHandleArgs(OftenObject, Object...)}）后调用此函数释放相关资源（如本地线程变量）
     */
    public void release()
    {
        threadLocalOfRequestData.remove();
        Stack stack = threadLocal.get();
        if (!stack.empty())
        {
            stack.pop();
        }

        //requestDataMap不能清除
    }

    public abstract OftenRequest getRequest();

    public abstract OftenResponse getResponse();

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


    public abstract Delivery delivery();

    public abstract UrlDecoder.Result url();

    public String contextName()
    {
        return url().contextName();
    }

    public String classTied()
    {
        return url().classTied();
    }

    public String funTied()
    {
        return url().funTied();
    }

    /**
     * 得到所属的接口框架名称。
     *
     * @return
     */
    public abstract BridgeName getBridgeName();

    public <T> T cnOf(int index)
    {
        T t = (T) _cn[index];
        return t;
    }

    public <T> T cuOf(int index)
    {
        T t = (T) _cu[index];
        return t;
    }

    public <T> T cuOf(int index, T defaultValue)
    {
        T t = (T) _cu[index];
        if (t == null)
        {
            t = defaultValue;
        }
        return t;
    }

    public <T> T fnOf(int index)
    {
        T t = (T) _fn[index];
        return t;
    }

    public <T> T fuOf(int index)
    {
        T t = (T) _fu[index];
        return t;
    }

    public <T> T fuOf(int index, T defaultValue)
    {
        T t = (T) _fu[index];
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
     * 获取非必须参数
     *
     * @param name
     * @param <T>
     * @return
     */
    public <T> T unece(String name, T defaultValue)
    {
        T t = getParamSource().getParam(name);
        if (OftenTool.isNullOrEmptyCharSequence(t))
        {
            t = defaultValue;
        }
        return t;
    }


    /**
     * 另见{@linkplain FunParam#toJSON(Object...)} ,
     * {@linkplain #innerRequest(PortMethod, String, String, Map, BridgeCallback, boolean)}.
     */
    public <T> T invokePorter(PortMethod method, String funTied, Object... objects)
    {
        return invokePorter(method, null, funTied, FunParam.toJSON(objects));
    }

    /**
     * 另见{@linkplain FunParam#toJSON(Object...)},
     * {@linkplain #innerRequest(PortMethod, String, String, Map, BridgeCallback, boolean)}.
     */
    public <T> T invokePorter(PortMethod method, String classTied, String funTied, Object... objects)
    {
        return invokePorter(method, classTied, funTied, FunParam.toJSON(objects));
    }

    /**
     * 另见{@linkplain FunParam#toJSON(Object...)},
     * {@linkplain #innerRequest(PortMethod, String, String, Map, BridgeCallback, boolean)}.
     */
    public <T> T invokePorter(PortMethod method, String classTied, String funTied, Map<String, Object> params)
    {
        Object[] temp = new Object[1];
        innerRequest(method, classTied, funTied, params, lResponse -> temp[0] = lResponse.getResponse(), false);
        Object rs = temp[0];
        return SyncPorterThrowsImpl.deal(rs);
    }


    /**
     * 使用当前请求的接口方法。
     *
     * @param funTied
     * @param params
     * @param callback
     */
    public void innerRequest(String funTied, Map<String, Object> params, BridgeCallback callback)
    {
        innerRequest(getRequest().getMethod(), null, funTied, params, callback, true);
    }

    /**
     * @param method              为null、等于当前的
     * @param funTied             为null、等于当前的
     * @param params
     * @param callback
     * @param throwWCallException 是否在返回码不为成功时抛出异常。
     */
    public void innerRequest(PortMethod method, String classTied, String funTied, Map<String, Object> params,
            BridgeCallback callback,
            boolean throwWCallException)
    {
        UrlDecoder.Result result = url();
        if (method == null)
        {
            method = getRequest().getMethod();
        }

        StringBuilder builder = new StringBuilder();
        builder.append('/').append(result.contextName()).append('/');
        if (OftenTool.notEmpty(classTied))
        {
            builder.append(classTied);
        } else
        {
            builder.append(result.classTied());
        }
        builder.append('/');
        builder.append(funTied == null ? result.funTied() : funTied);

        BridgeRequest request = BridgeRequest.withNewPath(this, builder.toString(), method, getRequest(), true);
        request.addParamAll(params);
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

    public OftenObject original()
    {
        OftenRequest request = getRequest();
        OftenObject originalObject = request == null ? null : request.getOriginalObject();
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
        OftenObject oftenObject = original();
        if (oftenObject.requestDataMap == null)
        {
            oftenObject.requestDataMap = new HashMap<>();
        }
        return (T) oftenObject.requestDataMap.put(name, value);
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

    public <T> T putLocalRequestData(Class<?> clazz, Object value)
    {
        return putLocalRequestData(clazz.getName(), value);
    }

    /**
     * 设置数据，只对当前线程有效。
     *
     * @param name  属性名
     * @param value 属性值
     * @return 返回上一次的值
     */
    public <T> T putLocalRequestData(String name, Object value)
    {
        LOGGER.debug("name={},value={}", name, value);
        Map<String, Object> map = threadLocalOfRequestData.get();
        return (T) map.put(name, value);
    }

    public <T> T getRequestData(Class<T> clazz)
    {
        return getRequestData(clazz.getName());
    }

    public <T> T getRequestData(String name)
    {
        OftenObject oftenObject = original();
        if (oftenObject.requestDataMap == null)
        {
            return null;
        }
        return (T) oftenObject.requestDataMap.get(name);
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

    public <T> T getLocalRequestData(Class<T> clazz)
    {
        return getLocalRequestData(clazz.getName());
    }


    public <T> T getLocalRequestData(String name)
    {
        return (T) threadLocalOfRequestData.get().get(name);
    }

    public <T> T removeLocalRequestData(Class<T> clazz)
    {
        return removeLocalRequestData(clazz.getName());
    }

    public <T> T removeLocalRequestData(String name)
    {
        return (T) threadLocalOfRequestData.get().remove(name);
    }

    public <T> T removeRequestData(String name)
    {
        OftenObject oftenObject = original();
        if (oftenObject.requestDataMap == null)
        {
            return null;
        }
        return (T) oftenObject.requestDataMap.remove(name);
    }

    private IListenerAdder<IFinalListener> listenerAdder;

    private IListenerAdder<IFinalListener> getListenerAdder(boolean willCreate)
    {
        OftenObject oftenObject = original();
        if (oftenObject.listenerAdder == null && willCreate)
        {
            oftenObject.listenerAdder = new DefaultListenerAdder<>();
        }
        return oftenObject.listenerAdder;
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

    @Override
    public IConfigData getConfigData()
    {
        return null;
    }
}
