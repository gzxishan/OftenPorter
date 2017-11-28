package cn.xishan.oftenporter.oftendb.annotation;

import java.lang.annotation.*;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface MyBatisField
{
    Class<?> value();
}
