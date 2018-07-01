package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.base.WObject;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Inherited
@Documented
public @interface AspectOperationOfNormal
{
    interface Handle<T extends Annotation>
    {
        boolean init(T current, Object object, Method method);

        Object onInvoke(@MayNull WObject wObject,Object object, Method method, Object[] args);

        boolean onEnd(@MayNull WObject wObject, Object object, Method method);
    }

    Class<? extends Handle> handle();
}
