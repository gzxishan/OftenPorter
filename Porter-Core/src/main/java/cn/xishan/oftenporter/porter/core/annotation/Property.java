package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.init.PorterConf;

import java.lang.annotation.*;

/**
 * 获取属性值,见{@linkplain PorterConf#setAnnotationConfig(Object)}
 * <p>
 * 支持的类型同{@linkplain IConfigData}
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018-07-20.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface Property
{
    String value();
}
