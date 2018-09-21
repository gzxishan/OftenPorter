package cn.xishan.oftenporter.porter.core.util.proxy;

import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import net.sf.cglib.proxy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
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

    public static <T> T newProxyInstance(ClassLoader loader,
            Class<?>[] interfaces,
            InvocationHandler h)
    {
        List<Class<?>> list = new ArrayList<>(interfaces.length + 1);
        WPTool.addAll(list, interfaces);
        list.add(__ProxyUtil_.class);
        interfaces = list.toArray(new Class[0]);

        Object obj = Proxy.newProxyInstance(loader, interfaces, h);
        return (T) obj;
    }

    public static Class unwrapProxyForGeneric(Class clazz)
    {
        clazz = PortUtil.getRealClass(clazz);
        if (CGLIB_NAME_PATTERN.matcher(clazz.getName()).find())
        {
            return clazz.getSuperclass();
        } else
        {
            return clazz;
        }
    }

    public static Object proxyObject(Object object, Class[] interfaces, IMethodFilter methodFilter,
            IInvocationable invocationable) throws Exception
    {
        Callback[] callbacks =
                new Callback[]{NoOp.INSTANCE, (MethodInterceptor) (obj, method, args, methodProxy) -> {

                    IInvocationable.IInvoker iInvoker = args1 -> methodProxy.invokeSuper(obj, args1);

                    return invocationable.invoke(iInvoker, object, method, args);
                }};

        Enhancer enhancer = new Enhancer();
        enhancer.setCallbacks(callbacks);
        enhancer.setCallbackFilter(method -> {
            if (methodFilter.contains(object, method))
            {
                return 1;
            } else
            {
                return 0;
            }
        });
        enhancer.setSuperclass(object.getClass());
        if (interfaces != null)
        {
            enhancer.setInterfaces(interfaces);
        }
        Object proxyObject = enhancer.create();
        initFieldsValue(object, proxyObject, true);
        return proxyObject;
    }


    public static void initFieldsValue(Object src, Object target, boolean fromSrc2target) throws Exception
    {
        if (src != null && target != null)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("copy fields from:[{}] to [{}]", src.getClass() + "@" + src.hashCode(),
                        target.getClass() + "@" + target.hashCode());
            }
            Field[] fields = WPTool.getAllFields(src.getClass());
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
