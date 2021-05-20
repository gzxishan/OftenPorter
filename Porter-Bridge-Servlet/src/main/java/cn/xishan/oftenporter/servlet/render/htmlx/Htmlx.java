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
public @interface Htmlx {

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
     * 实际访问的页面路径（规则同servlet）,会找到第一个存在的，如/mobile/index.html(或者/mobile/)、/mobile/*。
     *
     * @return
     */
    String[] path();

    /**
     * 若不为空、且{@linkplain #filePattern()}匹配、且其表示的文件({@linkplain HtmlxDoc#getRelativeResource(String, String)})存在，则无轮
     * {@linkplain #path()}是什么路径都会读取该文件。，
     *
     * @return
     */
    String file() default "";

    /**
     * 触发条件等于{@linkplain #file()}，但优先级更高。若以"/"开头且{@linkplain #isDispatcher()}为false，则会加上servlet上下文路径。
     *
     * @return
     */
    String dispatcher() default "";

    /**
     * 设置{@linkplain #dispatcher()}是否为服务器端重定向：true表示是，false表示进行客户端重定向。
     *
     * @return
     */
    boolean isDispatcher() default true;

    /**
     * 使用{@linkplain #file()}或{@linkplain #dispatcher()}的正则判断。
     *
     * @return
     */
    String filePattern() default "";


    /**
     * 是否对{@linkplain #isNotPattern()}的结果取反。
     *
     * @return
     */
    boolean isNotPattern() default false;

    /**
     * html文件的后缀名,当为通配符等情况、且匹配到不为该后缀名的请求时会忽略。
     *
     * @return
     */
    String[] htmlSuffix() default {"html", "htm"};

    /**
     * 对于同一个请求被多次匹配的情况，数值更小的会被执行。
     *
     * @return
     */
    int order() default 0;

    HtmlxDoc.ResponseType defaultResponseType() default HtmlxDoc.ResponseType.Normal;

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
    String otherwiseHtml() default "";

    /**
     * 设置x-frame-options头：
     * <p>
     * 标准类型：
     * </p>
     * <ol>
     *     <li>
     *         DENY:表示该页面不允许在frame中展示，即便是在相同域名的页面中嵌套也不允许。
     *     </li>
     *     <li>
     *         SAMEORIGIN:表示该页面可以在相同域名页面的frame中展示。
     *     </li>
     *     <li>
     *         ALLOW-FROM uri:表示该页面可以在指定来源的frame中展示。
     *     </li>
     * </ol>
     * <p>
     *     新增类型：
     * </p>
     * <ol>
     *     <li>
     *         SAMEHOST:表示协议、host一样（端口无需一样）时允许。(用到了op.servlet.cors.http2https配置;当没有Referer或者不匹配时时设置为SAMEORIGIN)
     *     </li>
     * </ol>
     *
     * @return
     */
    String xFrameOptions() default "";

    /**
     * 当调用方法捕获到异常时，响应的错误码。
     *
     * @return 为-1表示忽略。
     */
    int statusCodeForEx() default 500;
}
