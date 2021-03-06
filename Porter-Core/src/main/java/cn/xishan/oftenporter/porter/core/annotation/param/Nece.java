package cn.xishan.oftenporter.porter.core.annotation.param;

import cn.xishan.oftenporter.porter.core.advanced.ITypeParserOption;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.base.PortMethod;

import java.lang.annotation.*;

/**
 * 注解在函数、函数形参、类变量上,对各种访问类型变量都有效。支持{@linkplain ITypeParserOption}。
 *
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
@Inherited
@Documented
public @interface Nece
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

    /**
     * 是否调用{@linkplain OftenObject#getRequestData(String)}获取数据
     */
    boolean requestData() default false;

    /**
     * 转换为非必需参数。
     * <pre>
     *     若返回true且当前请求在{@linkplain #forMethods()}、{@linkplain #forClassTieds()} ()}或{@linkplain #forFunTieds()}
     *     ()}中时:
     *      注解的变量变为非必需参数。
     * </pre>
     *
     * @return 默认为false。
     */
    boolean toUnece() default false;

    PortMethod[] forMethods() default {};

    String[] forClassTieds() default {};

    String[] forFunTieds() default {};

    /**
     * 是否执行trim()操作
     * @return
     */
    boolean trim() default true;

    /**
     * 是否删除所有空白符
     * @return
     */
    boolean clearBlank()default false;

    /**
     * 删除字符的正则表达式
     *
     * @return
     */
    String deleteRegex() default "";
}
