package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 被标记的变量不能为null(同时字符串不能为"")。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@Inherited
public @interface NotNull
{
}
