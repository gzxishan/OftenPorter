package cn.xishan.oftenporter.servlet.render.htmlx;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.init.DealSharpProperties;
import cn.xishan.oftenporter.servlet.render.RenderPage;

import java.lang.annotation.*;
import java.util.Map;

/**
 * <p>
 * 用于修改html文件,注解在porter函数上,注解在函数上时提供默认的配置。
 * </p>
 * <ol>
 * <li>
 * 支持#参数,需要返回{@linkplain RenderPage}或Map，见{@linkplain DealSharpProperties#replaceSharpProperties(String, Map, String)}。
 * </li>
 * <li>
 * 函数支持的形参：{@linkplain OftenObject},{@linkplain HtmlxDoc}
 * </li>
 * </ol>
 *
 * @author Created by https://github.com/CLovinr on 2019-01-11.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@AspectOperationOfPortIn(handle = HtmlxHandle.class)
public @interface Htmlx
{

    /**
     * 是否为debug模式，当为(true,yes或1)时、每次都会加载模板。
     *
     * @return
     */
    String isDebug() default "false";

    /**
     * 默认目录，从当前servlet的上下文根目录开始,其中{@linkplain #path()}、{@linkplain #otherwisePage()} 支持相对路径，且相对于该目录。
     *
     * @return
     */
    String baseDir() default "/";

    /**
     * 实际访问的页面路径（只对porter函数有效）,会找到第一个存在的，如/mobile/index.html(或者/mobile/)。
     *
     * @return
     */
    String[] path();

    /**
     * html文件的后缀名,当为通配符等情况、且匹配到不为该后缀名的请求时会忽略。
     *
     * @return
     */
    String[] htmlSuffix() default {"html", "htm"};

    String index() default "index.html";

    /**
     * 对于同一个请求被多次匹配的情况，数值更小的会被执行。
     *
     * @return
     */
    int order() default 0;

    boolean enable() default true;

    /**
     * 缓存时间。
     *
     * @return
     */
    int cacheSeconds() default 24 * 3600;

    /**
     * 页面编码方式。
     *
     * @return
     */
    String encoding() default "utf-8";

    String contentType() default "text/html";

    String title() default "";

    String description() default "";

    String keywords() default "";

    /**
     * 未找到{@linkplain #path()}与当前请求对应的html文件时，尝试查找的html文件（可用于错误页面）。
     *
     * @return
     */
    String otherwisePage() default "";

    /**
     * {@linkplain #otherwisePage()}文件的编码方式,为空则等于{@linkplain #encoding()}
     *
     * @return
     */
    String otherwisePageEncoding() default "";

    /**
     * 未找到{@linkplain #path()}与当前请求对应的html文件、且未找到{@linkplain #otherwisePage()}页面时、默认的html内容。
     *
     * @return
     */
    String otherwiseHtml() default "<!DOCTYPE html><html><head>" +
            "<meta charset='UTF-8'>" +
            "<title></title>" +
            "<meta name='viewport' content='width=device-width,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no' " +
            "/>" +
            "</head>" +
            "<body><h1 style='text-align:center;margin:10% auto;'>OftenPorter Servlet Bridge</h1></body></html>";
}
