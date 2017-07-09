package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * Created by chenyg on 2017/1/6.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@Inherited
public @interface MayNull
{
    /**
     * 描述。
     *
     * @return
     */
    String value() default "";
}
