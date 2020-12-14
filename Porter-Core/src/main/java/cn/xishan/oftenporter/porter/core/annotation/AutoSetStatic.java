package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.init.PorterConf;

import java.lang.annotation.*;

/**
 * 会将指定的类添加到{@linkplain PorterConf#addStaticAutoSetClasses(Class[])},会获取父类上的。
 *
 * @author Created by https://github.com/CLovinr on 2018/11/17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@AdvancedAnnotation(enableCache = false)
public @interface AutoSetStatic
{
    Class[] value();
}
