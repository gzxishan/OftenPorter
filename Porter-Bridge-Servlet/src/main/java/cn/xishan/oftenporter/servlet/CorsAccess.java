package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.base.PortMethod;

import java.lang.annotation.*;

/**
 * 对跨域的控制，默认禁止跨域,增强对CSRF攻击的防御能力。
 * <ol>
 * <li>
 * op.servlet.cors.disable:默认为false，表示一律禁止跨域访问、但可通过该注解进行单独设置，为true时、并不会对跨域进行处理。
 * </li>
 * <li>
 * op.servlet.cors.http2https:默认为false
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
     * 允许跨域访问的方法,同时设置Access-Control-Allow-Methods。
     *
     * @return
     */
    PortMethod[] allowMethods() default {};

    /**
     * 是否允许跨域访问，默认false。
     *
     * @return
     */
    boolean enabled() default false;

    /**
     * 是否是自己进行跨域的处理，默认false。
     * @return
     */
    boolean isCustomer()default false;

    /**
     * 对Access-Control-Allow-Credentials的设置,默认false。
     *
     * @return
     */
    boolean allowCredentials() default false;

    /**
     * 对Access-Control-Allow-Origin的设置,默认""。
     *
     * @return
     */
    String allowOrigin() default "";


    /**
     * 对Access-Control-Expose-Headers的设置，默认为空。
     *
     * @return
     */
    String exposeHeaders() default "";

    /**
     * 对Access-Control-Allow-Headers的设置，默认为空。
     *
     * @return
     */
    String allowHeaders() default "";

    /**
     * 对Access-Control-Max-Age的设置，单位秒。
     *
     * @return
     */
    String maxAge() default "";

}
