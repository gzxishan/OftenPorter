package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.advanced.IAnnotationConfigable;
import cn.xishan.oftenporter.porter.core.util.proxy.InvocationHandlerWithCommon;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2018-06-29.
 */
class AnnoUtilDynamicAttrHandler extends InvocationHandlerWithCommon
{
    interface _Dynamic_Annotation_Str_Attrs_
    {

    }

    private Annotation origin;
    private IAnnotationConfigable iAnnotationConfigable;

    public AnnoUtilDynamicAttrHandler(Annotation origin,
            IAnnotationConfigable iAnnotationConfigable)
    {
        super(origin);
        this.origin = origin;
        this.iAnnotationConfigable = iAnnotationConfigable;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if (method.equals(TO_STRING_METHOD))
        {
            return origin.toString();
        } else if (method.equals(TYPE_METHOD))
        {
            return origin.annotationType();
        }
        return super.invoke(proxy, method, args);
    }

    @Override
    public Object invokeOther(Object proxy, Method method, Object[] args) throws Throwable
    {
        Object rs = method.invoke(origin, args);
        if (rs instanceof String)
        {
            return iAnnotationConfigable.getAnnotationStringValue((String) rs);
        } else if (rs instanceof String[])
        {
            String[] strs = (String[]) rs;
            for (int i = 0; i < strs.length; i++)
            {
                strs[i] = iAnnotationConfigable.getAnnotationStringValue(strs[i]);
            }
            return strs;
        } else
        {
            return rs;
        }
    }
}
