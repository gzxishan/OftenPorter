package cn.xishan.oftenporter.servlet.render.htmlx;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.init.DealSharpProperties;
import cn.xishan.oftenporter.servlet.render.RenderPage;

import java.lang.annotation.*;
import java.util.Map;

/**
 * <p>
 * 用于修改html文件,注解在porter函数上。
 * </p>
 * <ol>
 * <li>
 * 支持#参数,需要返回{@linkplain RenderPage}或Map，见{@linkplain DealSharpProperties#replaceSharpProperties(String, Map)}。
 * </li>
 * <li>
 * 函数支持的形参：{@linkplain OftenObject},{@linkplain HtmlxDoc}
 * </li>
 * </ol>
 *
 * @author Created by https://github.com/CLovinr on 2019-01-11.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@AspectOperationOfPortIn(handle = HtmlxHandle.class)
public @interface Htmlx
{
    /**
     * 实际的页面路径,会找到第一个存在的，如/mobile/index.html(或者/mobile/)。
     *
     * @return
     */
    String[] path();

    String index() default "index.html";

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
     * 未找到对应html文件时、默认的html内容。
     *
     * @return
     */
    String defaultHtml() default "<!DOCTYPE html><html><head>" +
            "<meta charset=\"UTF-8\">" +
            "<title></title>" +
            "<meta name=\"viewport\" " +
            "content=\"width=device-width,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no\" />" +
            "</head>" +
            "<body></body></html>";
}
