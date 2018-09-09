package cn.xishan.oftenporter.porter.core.util.proxy;

import cn.xishan.oftenporter.porter.core.advanced.PortUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;

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
}
