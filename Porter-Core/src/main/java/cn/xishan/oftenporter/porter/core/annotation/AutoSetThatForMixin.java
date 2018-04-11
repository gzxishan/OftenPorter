package cn.xishan.oftenporter.porter.core.annotation;


import java.lang.annotation.*;

/**
 * 用于接收对方传过来的变量，默认接收混入者接口实例,对父类变量也有效。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface AutoSetThatForMixin
{
    Class<?> value() default AutoSet.class;

    String key() default "";

    boolean required() default true;
}
