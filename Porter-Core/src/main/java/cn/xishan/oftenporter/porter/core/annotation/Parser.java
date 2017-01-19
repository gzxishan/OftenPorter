package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import cn.xishan.oftenporter.porter.core.init.CommonMain;

import java.lang.annotation.*;

/**
 * 用于标记类型转换的绑定关系。
 * <pre>
 *     注解在类上，是全局的，对所有类参数与函数参数有效。注解在函数上的优先级大于类上的。
 * </pre>
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
@Documented
public @interface Parser
{

    /**
     * 用于转换绑定。
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Inherited
    @Documented
    public @interface parse
    {
        /**
         * 需要转换的参数的名称。
         */
        String[] paramNames();

        /**
         * 1)首先会使用Class.getName()获取，同{@linkplain #parserName()};2)转换的类(需要有无参构造函数)。优先于{@linkplain #parserName()}。
         */
        Class<? extends ITypeParser> parser() default ITypeParser.class;

        /**
         * 全局转换类的绑定的名称。
         * <br>
         * 见{@linkplain CommonMain#addGlobalTypeParser(ITypeParser)}
         */
        String parserName() default "";
    }

    parse[] value();

}
