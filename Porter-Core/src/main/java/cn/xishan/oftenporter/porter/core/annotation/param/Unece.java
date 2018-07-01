package cn.xishan.oftenporter.porter.core.annotation.param;

import java.lang.annotation.*;

/**
 * 注解在函数、函数形参、类变量上,对各种访问类型变量都有效。
 *
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,ElementType.TYPE})
@Inherited
@Documented
@Repeatable(Uneces.class)
public @interface Unece
{
    /**
     * 如果与{@linkplain #varName()}都为空，表示使用变量的名称。
     *
     * @return
     */
    String value() default "";

    /**
     * 同{@linkplain #value()},但优先使用{@linkplain #value()}.
     *
     * @return
     */
    String varName() default "";
}
