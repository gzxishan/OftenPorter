package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.util.proxy.InvocationHandlerWithCommon;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2018-07-03.
 */
class AnnoUtilDynamicHandler extends InvocationHandlerWithCommon
{
    private InvocationHandler handler;
    private boolean handleCommon;
    private Class<?> annotationType;



    public AnnoUtilDynamicHandler(InvocationHandler handler, Class<?> annotationType, boolean handleCommon)
    {
        super(handler);
        this.handler = handler;
        this.annotationType = annotationType;
        this.handleCommon = handleCommon;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if (method.equals(TYPE_METHOD))
        {
            return annotationType;
        } else
        {
            if(method.equals(TO_STRING_METHOD)){
                return annotationType.getName()+"@@"+this.hashCode();
            }
            if (handleCommon)
            {
                return handler.invoke(proxy, method, args);
            } else
            {
                return super.invoke(proxy, method, args);
            }
        }
    }

    @Override
    public Object invokeOther(Object proxy, Method method, Object[] args) throws Throwable
    {
        return handler.invoke(proxy, method, args);
    }
}
