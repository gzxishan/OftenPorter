package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 用于添加注释
 * Created by https://github.com/CLovinr on 2017/7/22.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
@Documented
public @interface PortComment {
    String name() default "";

    String desc() default "";

    String auth() default "";
}
