package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.AutoSet.SetOk;
import cn.xishan.oftenporter.porter.core.init.PorterConf;

import java.lang.annotation.*;

/**
 * 获取属性值,见{@linkplain IConfigData}。
 * <p>
 * 支持的类型同{@linkplain IConfigData},当配置文件中不存在对应的属性时、从{@linkplain IConfigData#get(String)}途径获取。
 * </p>
 * <p>
 * 支持该注解的地方：{@linkplain PortStart},{@linkplain PortDestroy},{@linkplain SetOk},{@linkplain AutoSet}
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
     * 属性名。支持多个属性，用","隔开，会获取第一个非空的属性。
     *
     * @return
     */
    String value();

    /**
     * 默认值。
     *
     * @return
     */
    String defaultVal() default "";
}
