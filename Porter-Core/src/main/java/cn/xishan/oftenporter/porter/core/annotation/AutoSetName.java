package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 注解在类上，用于指定额外的AutoSet的名称,
 * 其他地方可用该名称注入对应的实例、但需要对应实例已经被注入过或已经被添加到context中。
 *
 * @author Created by https://github.com/CLovinr on 2018-12-21.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface AutoSetName
{
    String value();
}
