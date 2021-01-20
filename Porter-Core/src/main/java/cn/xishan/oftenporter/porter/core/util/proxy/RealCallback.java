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

    private Object targetRef;
    private boolean useRef;
    private IInvocationable invocationable;

    public RealCallback(boolean useRef, Object target, IInvocationable invocationable)
    {
        this.useRef = useRef;
        this.targetRef = useRef ? new WeakReference<>(target) : target;
        this.invocationable = invocationable;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
    {
        IInvocationable.IInvoker iInvoker = args1 -> methodProxy.invokeSuper(obj, args1);
        return invocationable.invoke(iInvoker, obj, getTarget(), method, args);
    }

    public Object getTarget()
    {
        Object object = useRef ? ((WeakReference) targetRef).get() : targetRef;
        if (object == null)
        {
            throw new RuntimeException("object is null" + (useRef ? " in WeakReference" : ""));
        } else
        {
            return object;
        }
    }

    public IInvocationable getInvocationable()
    {
        return invocationable;
    }
}
