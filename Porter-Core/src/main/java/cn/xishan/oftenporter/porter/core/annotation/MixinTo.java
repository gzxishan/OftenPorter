package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * <pre>
 *  只对接口类有效，用于混入某接口到其他接口类中。另见{@linkplain Mixin}
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2018/4/6.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
public @interface MixinTo
{
    /**
     * 是否覆盖。
     *
     * @return 默认为true
     */
    boolean override() default true;

    /**
     * 是否允许被其注解了的接口再被@{@linkplain Mixin}混入。
     *
     * @return 默认true
     */
    boolean enableMixin() default true;

    /**
     * 被混入的接口类，还可以是{@linkplain MixinTo}和{@linkplain MixinOnly}接口。
     */
    Class<?> toPorter();

    /**
     * 用于添加到context set列表。
     * @return 通过class设置key。
     */
    Class<?> toContextSetWithClassKey() default AutoSet.class;
}
