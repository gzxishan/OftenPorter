package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.base.PortMethod;

import java.lang.annotation.*;

/**
 * Created by chenyg on 2017-04-26.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface PorterSyncOption
{
    PortMethod method() default PortMethod.GET;

    /**
     * 默认为当前的context名称。
     *
     * @return
     */
    String context() default "";

    /**
     * 默认为当前接口的类绑定名。
     *
     * @return
     */
    String classTied() default "";

    /**
     * 用于获取类绑定名，优先于{@linkplain #classTied()}。
     *
     * @return
     */
    Class<?> porter() default PorterSyncOption.class;

    /**
     * 为空时，绑定名等于变量名。
     *
     * @return
     */
    String funTied() default "";
}
