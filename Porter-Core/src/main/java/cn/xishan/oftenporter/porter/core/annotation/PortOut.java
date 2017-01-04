package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.base.OutType;

import java.lang.annotation.*;

/**
 * 用于标记输出。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
@Documented
public @interface PortOut
{
    /**
     * 输出类型。
     */
    OutType value() default OutType.Object;
}
