package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 用于添加注释
 * Created by https://github.com/CLovinr on 2017/7/22.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
@Documented
public @interface PortComment
{
    /**
     * 接口名称
     *
     * @return
     */
    String name() default "";

    /**
     * 接口描述
     *
     * @return
     */
    String desc() default "";


    /**
     * 读写标识，默认为""。
     * <p>
     * 格式为："t1:t2:...:tx",比如"w:a"表示匿名可访问且含有写操作的接口，如用户注册。
     * </p>
     * <p>
     * 默认的几个操作符：
     * </p>
     * <ul>
     * <li>r：只有读操作的接口</li>
     * <li>w：有写操作的接口</li>
     * <li>a：匿名用户可访问的接口</li>
     * <li>u:登陆用户可访问的接口</li>
     * </ul>
     *
     * @return
     */
    String rw() default "";

    /**
     * 标签
     *
     * @return
     */
    String tag() default "";

    /**
     * 必须参数列表
     *
     * @return
     */
    String[] paramsNece() default {};

    /**
     * 非必须参数列表
     *
     * @return
     */
    String[] paramsUnece() default {};
}
