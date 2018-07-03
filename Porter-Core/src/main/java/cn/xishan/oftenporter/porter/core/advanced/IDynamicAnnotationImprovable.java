package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 用于动态注解的支持，需要加入资源文件/OP-INF/cn.xishan.oftenporter.porter.core.advanced
 * .IDynamicAnnotationImprovable，内容为实现类名称(每行一个).
 * <p>
 * 另见:{@linkplain AnnoUtil.Advanced}
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public interface IDynamicAnnotationImprovable
{
    class Result<T, A extends Annotation>
    {
        /**
         * 用于生成{@linkplain #appendAnnotation}对应的注解
         */
        public final T t;
        public final Class<A> appendAnnotation;
        private boolean willHandleCommonMethods = false;

        public Result(T t, Class<A> appendAnnotation)
        {
            this.t = t;
            this.appendAnnotation = appendAnnotation;
        }

        /**
         * 是否处理公共的方法：如hashCode,equals等
         *
         * @return 默认false
         */
        public boolean willHandleCommonMethods()
        {
            return willHandleCommonMethods;
        }


        public void setWillHandleCommonMethods(boolean willHandleCommonMethods)
        {
            this.willHandleCommonMethods = willHandleCommonMethods;
        }
    }

    class Adapter implements IDynamicAnnotationImprovable
    {

        @Override
        public Result<InvocationHandler, AspectOperationOfNormal> getAspectOperationOfNormal(Annotation annotation)
        {
            return null;
        }

        @Override
        public Result<InvocationHandler, AspectOperationOfPortIn> getAspectOperationOfPortIn(Annotation annotation)
        {
            return null;
        }

        @Override
        public Result<InvocationHandler, AutoSetDefaultDealt> getAutoSetDefaultDealt(Class<?> clazz)
        {
            return null;
        }

        @Override
        public <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Class<?> clazz,
                Class<A> annotationType)
        {
            return null;
        }

        @Override
        public <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Method method, Class<A> annotationType)
        {
            return null;
        }

        @Override
        public <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Field field, Class<A> annotationType)
        {
            return null;
        }

        @Override
        public <A extends Annotation> Result<InvocationHandler[], A> getRepeatableAnnotations(Class<?> clazz,
                Class<A> annotationType)
        {
            return null;
        }

        @Override
        public <A extends Annotation> Result<InvocationHandler[], A> getRepeatableAnnotations(Method method,
                Class<A> annotationType)
        {
            return null;
        }

        @Override
        public <A extends Annotation> Result<InvocationHandler[], A> getRepeatableAnnotations(Field field,
                Class<A> annotationType)
        {
            return null;
        }
    }

    Result<InvocationHandler, AspectOperationOfNormal> getAspectOperationOfNormal(Annotation annotation);

    Result<InvocationHandler, AspectOperationOfPortIn> getAspectOperationOfPortIn(Annotation annotation);

    Result<InvocationHandler, AutoSetDefaultDealt> getAutoSetDefaultDealt(Class<?> clazz);


    <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Class<?> clazz, Class<A> annotationType);

    <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Method method, Class<A> annotationType);

    <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Field field, Class<A> annotationType);

    <A extends Annotation> Result<InvocationHandler[], A> getRepeatableAnnotations(Class<?> clazz,
            Class<A> annotationType);

    <A extends Annotation> Result<InvocationHandler[], A> getRepeatableAnnotations(Method method,
            Class<A> annotationType);

    <A extends Annotation> Result<InvocationHandler[], A> getRepeatableAnnotations(Field field,
            Class<A> annotationType);
}
