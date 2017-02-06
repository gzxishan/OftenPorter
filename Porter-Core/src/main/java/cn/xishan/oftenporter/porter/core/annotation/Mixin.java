package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * <pre>
 * 用于混入其他接口。
 * 1.当注解在类上时，表示把对应接口类的所有接口方法混入，且当前被注解的类的接口方法优先、被混入的接口的类绑定名为当前类绑定名。
 * 2.混入的接口类本身可以混入其他接口类。（递归扫描）
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2017/1/19.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
public @interface Mixin
{
    /**
     * 将被混入的接口类。
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
