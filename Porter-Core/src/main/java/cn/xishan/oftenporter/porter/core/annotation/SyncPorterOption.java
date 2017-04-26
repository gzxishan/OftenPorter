package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.base.PortMethod;

import java.lang.annotation.*;

/**
 * Created by chenyg on 2017-04-26.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface SyncPorterOption
{
    PortMethod method() default PortMethod.GET;
    String context()default "";
    String classTied();
    String funTied();
}
