package cn.xishan.oftenporter.porter.core.annotation.param;

import java.lang.annotation.*;

/**
 * 进行json序列化。
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface JSONSerialize
{
    /**
     * 默认为"",表示使用变量名;否则使用该值。
     */
    String value() default "";

    boolean filterNullAndEmpty() default false;
}
