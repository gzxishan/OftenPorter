package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.util.WPTool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
public class AutoSetObjForAspectOfNormal
{
    public static class InvocationHandlerOfNormal implements InvocationHandler
    {
        private Object origin;

        public InvocationHandlerOfNormal(Object origin)
        {
            this.origin = origin;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            return method.invoke(origin, args);
        }
    }


    public AutoSetObjForAspectOfNormal()
    {
    }

    public Object doProxy(Object object)
    {
        if (Proxy.isProxyClass(object.getClass()))
        {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(object);
            if (invocationHandler instanceof InvocationHandlerOfNormal)
            {
                return object;
            }
        }

        Method[] methods = WPTool.getAllPublicMethods(object.getClass());
        for(Method method:methods){
            method.
        }

        InvocationHandlerOfNormal invocationHandlerOfNormal = new InvocationHandlerOfNormal(object);

        Object proxyObject = Proxy
                .newProxyInstance(object.getClass().getClassLoader(), object.getClass().getInterfaces(),
                        invocationHandlerOfNormal);

        return proxyObject;
    }
}
