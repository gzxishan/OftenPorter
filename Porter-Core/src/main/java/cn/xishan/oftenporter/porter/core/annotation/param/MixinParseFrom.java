package cn.xishan.oftenporter.porter.core.annotation.param;

import java.lang.annotation.*;

/**
 * 用于混入类型绑定{@linkplain Parse}。
 * <ol>
 * <li>支持递归扫描。</li>
 * <li>被混入的接口类的绑定优先于混入的类的类型绑定。</li>
 * <li>后加入的绑定覆盖之前的。</li>
 * </ol>
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
public @interface MixinParseFrom
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
