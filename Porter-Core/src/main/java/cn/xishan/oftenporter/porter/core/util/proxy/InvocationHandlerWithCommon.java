package cn.xishan.oftenporter.porter.core.util.proxy;

import cn.xishan.oftenporter.porter.core.exception.InitException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2018-07-03.
 */
public abstract class InvocationHandlerWithCommon implements InvocationHandler
{
    private Object forCommonObject;

    public static final Method TYPE_METHOD, TO_STRING_METHOD;
    static
    {
        try
        {
            TYPE_METHOD = Annotation.class.getDeclaredMethod("annotationType");
            TO_STRING_METHOD = Object.class.getMethod("toString");
        } catch (NoSuchMethodException e)
        {
            throw new InitException(e);
        }
    }

    public InvocationHandlerWithCommon(Object forCommonObject)
    {
        this.forCommonObject = forCommonObject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if (method.getDeclaringClass().equals(Object.class))
        {
            String name = method.getName();
            if (name.equals("hashCode") || name.equals("equals"))
            {
                return method.invoke(this, args);
            }
            return method.invoke(forCommonObject, args);
        }
        return invokeOther(proxy, method, args);
    }

    public abstract Object invokeOther(Object proxy, Method method, Object[] args) throws Throwable;
}
