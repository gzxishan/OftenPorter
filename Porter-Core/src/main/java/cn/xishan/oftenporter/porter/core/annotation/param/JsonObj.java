package cn.xishan.oftenporter.porter.core.annotation.param;

import java.lang.annotation.*;

/**
 * 用于对象转json时。
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface JsonObj
{
    /**
     * 默认为"",表示使用变量名;否则使用该值。
     */
    String value() default "";

    boolean filterNullAndEmpty() default false;

    /**
     * 请求时，是否设置内部变量
     *
     * @return
     */
    boolean willSetForRequest() default false;
}
