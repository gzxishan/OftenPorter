package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/10.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
public @interface MixinTos
{
    MixinTo[] value();
}
