package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 必需参数。
 * @author Created by https://github.com/CLovinr on 2018/5/12.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
public @interface NeceParam
{
    String value() default "";
}
