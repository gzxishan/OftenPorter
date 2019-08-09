package cn.xishan.oftenporter.porter.core.util.proxy;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2019-08-09.
 */
class RealCallback implements MethodInterceptor
{

    private WeakReference<Object> targetRef;
    private IInvocationable invocationable;

    public RealCallback(Object target, IInvocationable invocationable)
    {
        this.targetRef = new WeakReference<>(target);
        this.invocationable = invocationable;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
    {
        IInvocationable.IInvoker iInvoker = args1 -> methodProxy.invokeSuper(obj, args1);
        return invocationable.invoke(iInvoker, obj, targetRef.get(), method, args);
    }

    public Object getTarget()
    {
        return targetRef.get();
    }
}
