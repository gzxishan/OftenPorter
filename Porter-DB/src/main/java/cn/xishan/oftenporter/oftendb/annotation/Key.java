package cn.xishan.oftenporter.oftendb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by https://github.com/CLovinr on 2016/9/9.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Key
{
    /**
     * 默认为"",表示使用变量名;否则使用该值。
     *
     * @return 实际的数据库键或字段名
     */
    String value() default "";

    /**
     * 为null时，是否添加到数据库中去，默认为false。
     *
     * @return 为true，表示null值会被写到数据库；为false，则不会。
     */
    boolean nullSetOrAdd() default false;
}
