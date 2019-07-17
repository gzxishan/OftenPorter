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

    enum Choice
    {
        /**
         * 选择第一个存在的文件、如果都不存在则返回第一个，对应的值：1）为String多个用分号隔开，2）为String[]，3）为File[],4)为List(String)
         */
        FirstFile,
        /**
         * 选择第一个存在的目录、如果都不存在则返回第一个，对应的值：1）为String多个用分号隔开，2）为String[]，3）为File[],4)为List(String)
         */
        FirstDir,
        /**
         * 选择第一个存在的目录或文件、如果都不存在则返回第一个，对应的值：1）为String多个用分号隔开，2）为String[]，3）为File[],4)为List(String)
         */
        FirstFileDir,
        Default
    }

    /**
     * 属性名。支持多个属性，用","隔开，会获取第一个非空的属性。
     *
     * @return
     */
    String value() default "";

    /**
     * 同{@linkplain #value()}。
     *
     * @return
     */
    String name() default "";

    /**
     * 默认值。
     *
     * @return
     */
    String defaultVal() default "";

    Choice choice() default Choice.Default;
}
