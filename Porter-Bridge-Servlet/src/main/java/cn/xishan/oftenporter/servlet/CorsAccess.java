package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.base.PortMethod;

import java.lang.annotation.*;

/**
 * 对跨域的控制，默认禁止GET,POST,PUT,DELETE跨域。
 * <ol>
 * <li>
 * op.servlet.cors:默认为false，表示一律禁止跨域访问、但可通过该注解进行单独设置，为true时、并不会对跨域进行处理。
 * </li>
 * </ol>
 *
 * @author Created by https://github.com/CLovinr on 2018/8/19.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
public @interface CorsAccess
{
    /**
     * 禁止跨域访问的方法
     *
     * @return
     */
    PortMethod[] forbiddenMethods() default {PortMethod.GET, PortMethod.POST, PortMethod.PUT, PortMethod.DELETE};
}
