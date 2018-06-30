package cn.xishan.oftenporter.porter.core.annotation.param;

/**
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */

import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import cn.xishan.oftenporter.porter.core.init.CommonMain;

import java.lang.annotation.*;

/**
 * 用于转换绑定。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Inherited
@Documented
@Repeatable(Parses.class)
public @interface Parse
{
    /**
     * 需要转换的参数的名称。
     */
    String[] paramNames() default {};

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
