package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * @author Created by https://github.com/CLovinr on 2018-09-29.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Documented
@Advancable
public @interface Advancable
{
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
}
