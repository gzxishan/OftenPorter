package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.init.IAnnotationConfigable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2018-06-29.
 */
class AnnoUtilInvocationHandler extends InvocationHandlerWithCommon
{
    private Annotation origin;
    private IAnnotationConfigable iAnnotationConfigable;
    private Object config;

    public AnnoUtilInvocationHandler(Annotation origin,
            IAnnotationConfigable iAnnotationConfigable, Object config)
    {
        super(origin);
        this.origin = origin;
        this.iAnnotationConfigable = iAnnotationConfigable;
        this.config = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if (method.equals(TO_STRING_METHOD))
        {
            return origin.toString() + "@@" + this.hashCode();
        } else
        {
            return super.invoke(proxy, method, args);
        }
    }

    @Override
    public Object invokeOther(Object proxy, Method method, Object[] args) throws Throwable
    {
        Object rs = method.invoke(origin, args);
        if (rs instanceof String)
        {
            return iAnnotationConfigable.getValue(config, (String) rs);
        } else if (rs instanceof String[])
        {
            String[] strs = (String[]) rs;
            for (int i = 0; i < strs.length; i++)
            {
                strs[i] = iAnnotationConfigable.getValue(config, strs[i]);
            }
            return strs;
        } else
        {
            return rs;
        }
    }
}
