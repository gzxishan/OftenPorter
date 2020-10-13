package cn.xishan.oftenporter.porter.core.annotation.param;

import java.lang.annotation.*;

/**
 * @author Created by https://github.com/CLovinr on 2020-10-13.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
public @interface TYPE
{
    Class value();
}
