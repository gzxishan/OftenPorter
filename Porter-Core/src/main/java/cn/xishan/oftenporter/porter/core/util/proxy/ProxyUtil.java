package cn.xishan.oftenporter.porter.core.util.proxy;


import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetObjForAspectOfNormal;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import net.sf.cglib.proxy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Created by https://github.com/CLovinr on 2018-07-13.
 */
public class ProxyUtil
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyUtil.class);

    public interface __ProxyUtil_
    {

    }

    private static final Pattern CGLIB_NAME_PATTERN = Pattern.compile("\\$\\$[^\\s]*CGLIB[^\\s]*\\$\\$");

    /**
     * @param loader
     * @param interfaces
     * @param h          注意不要使用lambda表达式或局部匿名实例，防止内存泄漏。
     * @param <T>
     * @return
     */
    public static <T> T newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h)
    {
        List<Class<?>> list = new ArrayList<>(interfaces.length + 1);
        OftenTool.addAll(list, interfaces);
        list.add(__ProxyUtil_.class);
        interfaces = list.toArray(new Class[0]);

        Object obj = Proxy.newProxyInstance(loader, interfaces, h);
        return (T) obj;
    }


    public static Class unwrapProxyForGeneric(Object object)
    {
        return unwrapProxyForGeneric(object.getClass());
    }

    public static Class unwrapProxyForGeneric(Class clazz)
    {
        //clazz = PortUtil.getRealClass(clazz);
        if (CGLIB_NAME_PATTERN.matcher(clazz.getName()).find())
        {
            return clazz.getSuperclass();
        } else
        {
            return clazz;
        }
    }

    public static Object getRealObject(Object object)
    {
        String fieldName = "CGLIB$CALLBACK_1";
        try
        {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            if (object instanceof AutoSetObjForAspectOfNormal.IOPProxy)
            {
                Object methodInterceptor = field.get(object);
                field = methodInterceptor.getClass().getDeclaredField("originRef");
                field.setAccessible(true);
                WeakReference reference = (WeakReference) field.get(object);
                object = reference.get();
            } else if (object instanceof IOftenProxy)
            {
                RealCallback realCallback = (RealCallback) field.get(object);
                object = realCallback.getTarget();
            }
        } catch (NoSuchFieldException e)
        {

        } catch (Throwable e)
        {
            LOGGER.warn(e.getMessage(), e);
        }
        return object;
    }

    /**
     * 另见{@linkplain #proxyObject(Object, boolean, Class[], IMethodFilter, IInvocationable, ICGLIBSettable)}
     * @param proxyObject
     * @return
     */
    public static IInvocationable getIInvocationable(Object proxyObject)
    {
        IInvocationable iInvocationable = null;
        try
        {
            if (proxyObject instanceof IOftenProxy)
            {
                String fieldName = "CGLIB$CALLBACK_1";
                Field field = proxyObject.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);

                RealCallback realCallback = (RealCallback) field.get(proxyObject);
                iInvocationable = realCallback.getInvocationable();
            }
        } catch (NoSuchFieldException e)
        {

        } catch (Throwable e)
        {
            LOGGER.warn(e.getMessage(), e);
        }
        return iInvocationable;
    }

    /**
     * @param object
     * @param useCache
     * @param interfaces
     * @param methodFilter   注意不要使用lambda表达式或局部匿名实例，防止内存泄漏。
     * @param invocationable 注意不要使用lambda表达式或局部匿名实例，防止内存泄漏。
     * @return
     * @throws Exception
     */
    public static Object proxyObject(Object object, boolean useCache, Class[] interfaces, IMethodFilter methodFilter,
            IInvocationable invocationable, ICGLIBSettable settable) throws Exception
    {
        Enhancer enhancer = proxySetting(object, useCache, interfaces, methodFilter, invocationable, settable);
        Object proxyObject = enhancer.create();
        initFieldsValue(object, proxyObject, true);
        return proxyObject;
    }

    /**
     * @param object
     * @param useCache
     * @param interfaces
     * @param methodFilter   注意不要使用lambda表达式或局部匿名实例，防止内存泄漏。
     * @param invocationable 注意不要使用lambda表达式或局部匿名实例，防止内存泄漏。
     * @return
     * @throws Exception
     */
    public static Class proxyClass(Object object, boolean useCache, Class[] interfaces, IMethodFilter methodFilter,
            IInvocationable invocationable, ICGLIBSettable settable) throws Exception
    {
        Enhancer enhancer = proxySetting(object, useCache, interfaces, methodFilter, invocationable, settable);
        return enhancer.createClass();
    }

    private static Enhancer proxySetting(Object object, boolean useCache, Class[] interfaces,
            IMethodFilter methodFilter, IInvocationable invocationable, ICGLIBSettable settable) throws Exception
    {
        Callback[] callbacks =
                new Callback[]{NoOp.INSTANCE, new RealCallback(object, invocationable)};

        Enhancer enhancer = new Enhancer();
        enhancer.setCallbacks(callbacks);
        enhancer.setUseCache(useCache);
        enhancer.setCallbackFilter(new CallbackFilterImpl(object.getClass(), methodFilter));
        enhancer.setSuperclass(object.getClass());
        List<Class> list = new ArrayList<>();
        list.add(IOftenProxy.class);
        if (interfaces != null)
        {
            OftenTool.addAll(list, interfaces);
        }
        enhancer.setInterfaces(list.toArray(new Class[0]));
        if (settable != null)
        {
            settable.doSet(enhancer);
        }
        return enhancer;
    }


    /**
     * 复制成员变量值（除了static但包括final类型）,变量以src中的变量为准。
     *
     * @param src
     * @param target
     * @param fromSrc2target true表示从src复制到target；false表示从target复制到src。
     * @throws Exception
     */
    public static void initFieldsValue(Object src, Object target, boolean fromSrc2target) throws Exception
    {
        if (src != null && target != null)
        {
            if (LOGGER.isDebugEnabled())
            {
                if (fromSrc2target)
                {
                    LOGGER.debug("copy fields from:[{}] to [{}]", src.getClass() + "@" + src.hashCode(),
                            target.getClass() + "@" + target.hashCode());
                } else
                {
                    LOGGER.debug("copy fields from:[{}] to [{}]", target.getClass() + "@" + target.hashCode(),
                            src.getClass() + "@" + src.hashCode());
                }
            }
            Field[] fields = OftenTool.getAllFields(src.getClass());
            for (Field field : fields)
            {
                if (Modifier.isStatic(field.getModifiers()))
                {
                    continue;
                }
                field.setAccessible(true);
                if (fromSrc2target)
                {
                    field.set(target, field.get(src));
                } else
                {
                    field.set(src, field.get(target));
                }
            }
        }
    }
}
