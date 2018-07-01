package cn.xishan.oftenporter.porter.core.annotation.param;

import java.lang.annotation.*;

/**
 * 作为json的一个属性。
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface JsonField
{
    /**
     * 默认为"",表示使用变量名;否则使用该值。
     */
    String value() default "";

    boolean filterNullAndEmpty() default false;
}
