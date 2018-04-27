package cn.xishan.oftenporter.servlet;

import java.lang.annotation.*;

/**
 * 忽略对multipart表单的处理。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface IgnoreDefaultMultipart
{

}
