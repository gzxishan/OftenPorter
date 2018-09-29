package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Created by https://github.com/CLovinr on 2018-09-29.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Documented
@AdvancedAnnotation
public @interface AdvancedAnnotation
{

    interface Handle<A extends Annotation>
    {
        A onGotAnnotation(@MayNull Class clazz, @MayNull Method method, @MayNull Field field, A annotation);
    }

    /**
     * 是否开启高级获取注解，默认false。
     *
     * @return
     */
    boolean enableAdvancedAnnotation() default false;

    /**
     * 是否开启缓存，默认true。
     *
     * @return
     */
    boolean enableCache() default true;

    Class<? extends Handle> handle() default Handle.class;
}
