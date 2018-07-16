package cn.xishan.oftenporter.oftendb.annotation;

import java.lang.annotation.*;

/**
 * 注解在Dao上。
 * @author Created by https://github.com/CLovinr on 2018-07-16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@Repeatable(MyBatisAliass.class)
public @interface MyBatisAlias
{
    Class<?> type();

    String alias() default "";
}
