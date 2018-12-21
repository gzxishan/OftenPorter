package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 用于指定额外的AutoSet的名称。
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
