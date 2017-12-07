package cn.xishan.oftenporter.porter.core.annotation;


import java.lang.annotation.*;

/**
 * 用于接收对方传过来的变量。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface AutoSetThatForMixin
{
    Class<?> value() default AutoSetThatForMixin.class;

    String key() default "";

    boolean required() default true;
}
