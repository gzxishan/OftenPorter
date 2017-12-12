package cn.xishan.oftenporter.oftendb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于对象转json时。
 * Created by https://github.com/CLovinr on 2016/9/9.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonObj
{
    /**
     * 默认为"",表示使用变量名;否则使用该值。
     */
    String value() default "";

    boolean filterNullAndEmpty() default false;
}
