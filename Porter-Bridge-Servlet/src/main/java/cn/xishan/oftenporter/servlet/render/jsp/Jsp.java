package cn.xishan.oftenporter.servlet.render.jsp;

import cn.xishan.oftenporter.porter.core.annotation.AspectFunOperation;
import cn.xishan.oftenporter.servlet.render.RenderPage;

import java.lang.annotation.*;

/**
 * <pre>
 * 1.函数的返回值为{@linkplain RenderPage}或String(pagePath)。
 * 2.通过注入{@linkplain JspOption}进行全局配置。
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2017/11/27.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@AspectFunOperation(handle = JspHandle.class)
public @interface Jsp
{
    /**
     * 为""时，使用全局的。
     *
     * @return
     */
    String prefix() default "";


    /**
     * 为空时，使用全局的。
     *
     * @return
     */
    String suffix() default "";

    /**
     * -1表示使用全局配置，0表示false，非0表示true。
     *
     * @return
     */
    int useStdTag() default -1;

    /**
     * -1表示使用全局配置，0表示false，非0表示true。
     *
     * @return
     */
    int enableEL() default -1;

    /**
     * 为""表示使用全局配置
     *
     * @return
     */
    String pageEncoding() default "";

    /**
     * 不为""时添加，添加在{@linkplain JspOption#appendJspContent}之后。
     *
     * @return
     */
    String appendJspContent() default "";
}
