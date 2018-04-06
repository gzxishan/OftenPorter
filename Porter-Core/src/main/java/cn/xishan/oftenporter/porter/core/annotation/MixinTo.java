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
     * 是否覆盖，默认为true。
     *
     * @return
     */
    boolean override() default true;

    /**
     * 被混入的接口类，还可以是{@linkplain MixinTo}和{@linkplain MixinOnly}接口。
     *
     * @return
     */
    Class<?> porter();
}
