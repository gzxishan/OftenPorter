package cn.xishan.oftenporter.porter.core.annotation.param;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;

import java.lang.annotation.*;

/**
 * 注解在@{@linkplain PortIn}类上
 *
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
public @interface OnPorterEntity
{
    Class<?>[] value() default {};
}
