package cn.xishan.oftenporter.porter.core.advanced;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.AutoSetDefaultDealt;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;

/**
 * 用于动态注解的支持，需要加入资源文件/OP-INF/cn.xishan.oftenporter.porter.core.advanced
 * .IDynamicAnnotationImprovable，内容为实现类名称(每行一个)
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

        public Result(T t, Class<A> appendAnnotation)
        {
            this.t = t;
            this.appendAnnotation = appendAnnotation;
        }
    }

    Result<InvocationHandler, AspectOperationOfNormal> getAspectOperationOfNormal(Annotation annotation);

    Result<InvocationHandler, AspectOperationOfPortIn> getAspectOperationOfPortIn(Annotation annotation);

    Result<InvocationHandler, AutoSetDefaultDealt> getAutoSetDefaultDealt(Class<?> clazz);


//    <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Class<?> clazz);
//
//    <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Method method);
//
//    <A extends Annotation> Result<InvocationHandler, A> getAnnotation(Field field);
//
//    <A extends Annotation> Result<InvocationHandler[], A> getRepeatableAnnotations(Class<?> clazz, Annotation[]
// original);
//
//    <A extends Annotation> Result<InvocationHandler[], A> getRepeatableAnnotations(Method method, Annotation[]
// original);
//
//    <A extends Annotation> Result<InvocationHandler[], A> getRepeatableAnnotations(Field field, Annotation[]
// original);
}
