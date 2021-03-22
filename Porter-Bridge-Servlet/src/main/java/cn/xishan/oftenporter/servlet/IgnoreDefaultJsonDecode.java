package cn.xishan.oftenporter.servlet;

import java.lang.annotation.*;

/**
 * 忽略对json的默认处理，见{@linkplain OftenInitializer.BuilderBefore#setMultiPartOption(MultiPartOption)}。
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface IgnoreDefaultJsonDecode
{

}
