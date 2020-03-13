package cn.xishan.oftenporter.porter.core.util.proxy;

import net.sf.cglib.proxy.CallbackFilter;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2020/3/13.
 */
class CallbackFilterImpl implements CallbackFilter
{
    private WeakReference<Class> targetRef;
    private IMethodFilter methodFilter;

    public CallbackFilterImpl(Class clazz, IMethodFilter filter)
    {
        this.targetRef = new WeakReference<>(clazz);
        this.methodFilter = filter;
    }

    @Override
    public int accept(Method method)
    {
        Class clazz = targetRef.get();
        if (clazz == null)
        {
            throw new RuntimeException("clazz is null in WeakReference");
        } else
        {
            if (methodFilter.contains(clazz, method))
            {
                return 1;
            } else
            {
                return 0;
            }
        }
    }

    private Class getClazz()
    {
        Class clazz = targetRef.get();
        if (clazz == null)
        {
            throw new RuntimeException("clazz is null in WeakReference");
        } else
        {
            return clazz;
        }
    }

    @Override
    public int hashCode()
    {
        return methodFilter.getHashCode(getClazz());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof CallbackFilterImpl)
        {
            CallbackFilterImpl filter = (CallbackFilterImpl) obj;
            return methodFilter.isEquals(getClazz(), filter.methodFilter, filter.getClazz());
        } else
        {
            return false;
        }

    }
}
