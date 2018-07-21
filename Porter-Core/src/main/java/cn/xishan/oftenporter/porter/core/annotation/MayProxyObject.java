package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 可能是代理对象。
 * Created by chenyg on 2017/1/6.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@Inherited
public @interface MayProxyObject
{
    /**
     * 描述。
     *
     * @return
     */
    String value() default "";
}
