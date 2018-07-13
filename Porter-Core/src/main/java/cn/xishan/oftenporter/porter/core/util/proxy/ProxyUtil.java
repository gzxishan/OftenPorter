package cn.xishan.oftenporter.porter.core.util.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author Created by https://github.com/CLovinr on 2018-07-13.
 */
public class ProxyUtil
{
    public static <T> T newProxyInstance(ClassLoader loader,
            Class<?>[] interfaces,
            InvocationHandler h)
    {
        Object obj = Proxy.newProxyInstance(loader, interfaces, h);
        return (T) obj;
    }
}
