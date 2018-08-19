package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.init.PorterConf;

import java.lang.annotation.*;

/**
 * 获取属性值,见{@linkplain PorterConf#setAnnotationConfig(Object)}
 * <p>
 * 支持的类型同{@linkplain IConfigData},当配置文件中不存在对应的属性时、从{@linkplain IConfigData#set(String, Object)}途径获取。
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018-07-20.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface Property
{
    /**
     * 属性名
     * @return
     */
    String value();

    /**
     * 默认值
     * @return
     */
    String defaultVal() default "";
}
