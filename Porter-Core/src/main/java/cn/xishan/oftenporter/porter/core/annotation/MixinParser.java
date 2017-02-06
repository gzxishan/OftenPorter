package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 用于混入类型绑定{@linkplain Parser.parse}。
 * <pre>
 * 1.不支持递归扫描。
 * 2.被混入的接口类的绑定优先于混入的类的类型绑定。
 * </pre>
 * @author Created by https://github.com/CLovinr on 2017/2/6.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
public @interface MixinParser
{
    /**
     * 将混入这些类的类型绑定。
     *
     * @return
     */
    Class<?>[] porters() default {};

    /**
     * 优先于{@linkplain #porters()}。
     *
     * @return
     */
    Class<?>[] value() default {};
}
