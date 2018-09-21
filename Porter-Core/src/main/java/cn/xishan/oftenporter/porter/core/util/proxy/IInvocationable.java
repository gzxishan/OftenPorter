package cn.xishan.oftenporter.porter.core.util.proxy;

import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2018/9/20.
 */
public interface IInvocationable
{
    public interface IInvoker
    {
        Object doInvoke(Object... args)throws Throwable;
    }

    Object invoke(IInvoker proxyInvoker,Object proxy, Object origin, Method originMethod, Object[] args)throws Throwable;
}
