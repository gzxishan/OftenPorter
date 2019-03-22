package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.advanced.DefaultReturnFactory;
import cn.xishan.oftenporter.porter.core.init.PorterConf;

import java.lang.annotation.*;

/**
 * 用于设置输出方式,注解在类上且函数没有该注解时作为函数的默认输出注解。
 * <p>
 * 当类或函数上没有该注解时：
 * <ol>
 * <li>
 * 当类上没有时:等效于@PortOut(OutType.AUTO),也可通过{@linkplain PorterConf#setDefaultPortOutType(OutType)}来设置默认的输出方式。
 * </li>
 * <li>
 * 对于函数，如果返回类型为void，则输出方式为{@linkplain OutType#VoidReturn}。
 * </li>
 * <li>
 * 对于函数，如果返回类型不是void，则输出方式等于类上的输出方式。
 * </li>
 * </ol>
 * </p>
 * <p>
 * 另见：{@linkplain PorterConf#setDefaultReturnFactory(DefaultReturnFactory) PorterConf.setDefaultReturnFactory
 * (DefaultReturnFactory)},
 * {@linkplain PorterConf#setDefaultPortOutType(OutType) PorterConf.setDefaultPortOutType(OutType)}
 * </p>
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Documented
public @interface PortOut
{
    /**
     * 输出类型,默认为{@linkplain OutType#AUTO},但可以通过{@linkplain PorterConf#setDefaultPortOutType(OutType)}来设置context级别的默认输出类别。
     */
    OutType value() default OutType.AUTO;
}
