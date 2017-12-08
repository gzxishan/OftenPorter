package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetDealt;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;

import java.lang.annotation.*;

/**
 * 对注入进行处理.
 *
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface AutoSetDefaultDealt
{
    /**
     * 默认选项，当{@linkplain AutoSet#option() AutoSet.option()}为空时有效。
     *
     * @return
     */
    String option() default "";

    /**
     * 对应的AutoSetDealt必须含有无参构造函数。
     *
     * @return
     */
    Class<? extends AutoSetDealt> dealt() default AutoSetDealt.class;

    /**
     * 对应的AutoSetGen必须含有无参构造函数。
     *
     * @return
     */
    Class<? extends AutoSetGen> gen() default AutoSetGen.class;
}
