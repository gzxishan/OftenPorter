package cn.xishan.oftenporter.porter.core.annotation.deal;

import cn.xishan.oftenporter.porter.core.advanced.IDynamicAnnotationImprovable;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import com.sun.corba.se.impl.oa.NullServantImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by https://github.com/CLovinr on 2018-07-13.
 */
class DynamicAnnotationImprovableWrap implements IDynamicAnnotationImprovable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicAnnotationImprovableWrap.class);
    private String classType;
    private IDynamicAnnotationImprovable real;

    private static boolean useWhiteOrDeny = false;
    private static final Set<String> deniedSet = ConcurrentHashMap.newKeySet();
    private static final Set<String> whiteSet = ConcurrentHashMap.newKeySet();

    public DynamicAnnotationImprovableWrap(String classType)
    {
        this.classType = classType;
        LOGGER.debug("add IDynamicAnnotationImprovable:{}", classType);
    }

    public static boolean isUseWhiteOrDeny()
    {
        return useWhiteOrDeny;
    }

    public static void setUseWhiteOrDeny(boolean useWhiteOrDeny)
    {
        DynamicAnnotationImprovableWrap.useWhiteOrDeny = useWhiteOrDeny;
    }

    public static void addDeny(String className)
    {
        deniedSet.add(className);
    }

    public static void removeDeny(String className)
    {
        deniedSet.remove(className);
    }

    public static void addWhite(String className)
    {
        whiteSet.add(className);
    }

    public static void removeWhite(String className)
    {
        whiteSet.remove(className);
    }

    private IDynamicAnnotationImprovable getReal()
    {
        if (useWhiteOrDeny)
        {
            if (!whiteSet.contains(classType))
            {
                return null;
            }
        } else
        {
            if (deniedSet.contains(classType))
            {
                return null;
            }

        }

        if (real == null)
        {
            try
            {
                IDynamicAnnotationImprovable iDynamicAnnotationImprovable = WPTool.newObject(classType);
                real = iDynamicAnnotationImprovable;
            } catch (Exception e)
            {
                LOGGER.warn(e.getMessage(), e);
            }
        }
        return real;
    }

    @Override
    public Result<InvocationHandler, AspectOperationOfNormal> getAspectOperationOfNormal(Annotation annotation)
    {
        IDynamicAnnotationImprovable improvable = getReal();
        return improvable == null ? null : improvable.getAspectOperationOfNormal(annotation);
    }

    @Override
    public Result<InvocationHandler, AspectOperationOfPortIn> getAspectOperationOfPortIn(Annotation annotation)
    {
        IDynamicAnnotationImprovable improvable = getReal();
        return improvable == null ? null : improvable.getAspectOperationOfPortIn(annotation);
    }

    @Override
    public Result<InvocationHandler, AutoSetDefaultDealt> getAutoSetDefaultDealt(Class<?> clazz)
    {
        IDynamicAnnotationImprovable improvable = getReal();
        return improvable == null ? null : improvable.getAutoSetDefaultDealt(clazz);
    }

    @Override
    public <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Class<?> clazz, Class<A> annotationType)
    {
        IDynamicAnnotationImprovable improvable = getReal();
        return improvable == null ? null : improvable.getAnnotation(clazz, annotationType);
    }

    @Override
    public <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Method method, Class<A> annotationType)
    {
        IDynamicAnnotationImprovable improvable = getReal();
        return improvable == null ? null : improvable.getAnnotation(method, annotationType);
    }

    @Override
    public <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Field field, Class<A> annotationType)
    {
        IDynamicAnnotationImprovable improvable = getReal();
        return improvable == null ? null : improvable.getAnnotation(field, annotationType);
    }

    @Override
    public <A extends Annotation> Result<InvocationHandler[], A> getRepeatableAnnotations(Class<?> clazz,
            Class<A> annotationType)
    {
        IDynamicAnnotationImprovable improvable = getReal();
        return improvable == null ? null : improvable.getRepeatableAnnotations(clazz, annotationType);
    }

    @Override
    public <A extends Annotation> Result<InvocationHandler[], A> getRepeatableAnnotations(Method method,
            Class<A> annotationType)
    {
        IDynamicAnnotationImprovable improvable = getReal();
        return improvable == null ? null : improvable.getRepeatableAnnotations(method, annotationType);
    }

    @Override
    public <A extends Annotation> Result<InvocationHandler[], A> getRepeatableAnnotations(Field field,
            Class<A> annotationType)
    {
        IDynamicAnnotationImprovable improvable = getReal();
        return improvable == null ? null : improvable.getRepeatableAnnotations(field, annotationType);
    }
}
