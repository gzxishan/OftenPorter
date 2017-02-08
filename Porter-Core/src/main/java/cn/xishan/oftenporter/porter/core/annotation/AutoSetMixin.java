package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 用于在混入接口类间传递变量，只对接口类变量有效,优先于{@linkplain AutoSet}注解。
 *
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface AutoSetMixin
{
    /**
     * 为""表示匹配名称为当前注解类的Class.getName，从被混入的类中查找有该注解的对应变量.
     *
     * @return
     */
    String value() default "";

    /**
     * 作为{@linkplain #value()}的补充，当{@linkplain #value()}不为""且其值不为默认值时，则使用classValue().getName()作为匹配名称。
     *
     * @return
     */
    Class<?> classValue() default AutoSetMixin.class;

    /**
     * 是否等待被设置,默认true。
     * @return
     */
    boolean waitingForSet() default true;
}
