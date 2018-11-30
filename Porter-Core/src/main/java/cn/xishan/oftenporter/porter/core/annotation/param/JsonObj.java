package cn.xishan.oftenporter.porter.core.annotation.param;

import cn.xishan.oftenporter.porter.core.base.FilterEmpty;

import java.lang.annotation.*;

/**
 * 用于把对象转json时，只对含有指定注解的变量有效：如{@linkplain JsonField},{@linkplain JsonObj}等。
 *
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

    /**
     * 是否过滤内部的空field。
     *
     * @return
     */
    FilterEmpty filterNullAndEmpty() default FilterEmpty.AUTO;

    /**
     * 请求时，是否设置内部变量
     *
     * @return
     */
    boolean willSetForRequest() default false;
}
