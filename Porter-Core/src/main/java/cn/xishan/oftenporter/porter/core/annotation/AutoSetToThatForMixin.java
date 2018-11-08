package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 用于把变量传递给对方,对父类变量也有效,当没有使用@{@linkplain AutoSet}注解变量时、相当于注解了@{@linkplain AutoSet}(nullAble=true)。
 * <p>
 *     默认情况下key为变量类型全名。
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2017/2/7.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface AutoSetToThatForMixin
{
    Class<?> value() default AutoSet.class;

    String key() default "";
}
