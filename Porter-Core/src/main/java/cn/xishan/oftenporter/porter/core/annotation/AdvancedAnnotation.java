package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.advanced.IDynamicAnnotationImprovable;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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
        A onGotAnnotation(@MayNull Class clazz, @MayNull Method method, @MayNull Parameter parameter,
                @MayNull Field field, A annotation);
    }

    /**
     * 是否开启高级获取注解的选项，默认false。另见:{@linkplain AnnoUtil.Advance},{@linkplain IDynamicAnnotationImprovable}
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

    /**
     *  是否缓存注解
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Inherited
    @Documented
    @AdvancedAnnotation
    @interface EnableCache
    {
        /**
         * 是否开启缓存，默认true。
         */
        boolean enable() default true;
    }
}
