package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.base.OutType;
import cn.xishan.oftenporter.porter.core.base.DefaultReturnFactory;
import cn.xishan.oftenporter.porter.core.init.PorterConf;

import java.lang.annotation.*;

/**
 * 用于标记输出,注解在类上且函数没有该注解时作为函数的默认输出注解。
 * <pre>
 *     另见：{@linkplain PorterConf#setDefaultReturnFactory(DefaultReturnFactory) PorterConf.setDefaultReturnFactory(DefaultReturnFactory)}
 *           {@linkplain PorterConf#setDefaultPortOutType(OutType) PorterConf.setDefaultPortOutType(OutType)}
 * </pre>
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Documented
public @interface PortOut {
    /**
     * 输出类型,默认为{@linkplain OutType#AUTO},但可以通过{@linkplain PorterConf#setDefaultPortOutType(OutType)}来设置context级别的默认输出类别。
     */
    OutType value() default OutType.AUTO;
}
