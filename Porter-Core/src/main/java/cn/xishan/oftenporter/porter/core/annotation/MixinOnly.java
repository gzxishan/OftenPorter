package cn.xishan.oftenporter.porter.core.annotation;


import java.lang.annotation.*;

/**
 * 加在接口类上，表示只用于混入。
 *
 * @author Created by https://github.com/CLovinr on 2017/3/4.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
public @interface MixinOnly
{
    /**
     * 是否覆盖被混入类的接口方法，默认为false。
     *
     * @return
     */
    boolean override() default false;
}
