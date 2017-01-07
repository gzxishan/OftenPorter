package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 被注解的类必须含有无参构造函数。
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface AutoSetSeek
{
}
