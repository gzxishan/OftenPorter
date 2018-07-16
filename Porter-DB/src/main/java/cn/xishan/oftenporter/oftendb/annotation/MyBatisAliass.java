package cn.xishan.oftenporter.oftendb.annotation;

import java.lang.annotation.*;

/**
 * @author Created by https://github.com/CLovinr on 2018-07-16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface MyBatisAliass
{
    MyBatisAlias[] value();
}
