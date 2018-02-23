package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 用于把变量传递给对方,对父类变量也有效。
 *
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface AutoSetToThatForMixin
{
    Class<?> value() default AutoSetToThatForMixin.class;

    String key() default "";
}
