package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.init.PorterConf;

import java.lang.annotation.*;

/**
 * 用于导入自定义的配置，对应的类可以实现以下接口：
 * <ol>
 * <li>
 * {@linkplain Configable}
 * </li>
 * </ol>
 * <p>
 * 另见：{@linkplain ImportProperties}
 * </p>
 *
 * @author Created by https://github.com/CLovinr on 2018-10-29.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Inherited
@Documented
public @interface Importer
{
    Class[] value();

    boolean exceptionThrow() default true;

    interface Configable<A extends Annotation>
    {
        void beforeCustomerConfig(PorterConf porterConf,A annotation)throws Throwable;
    }
}
