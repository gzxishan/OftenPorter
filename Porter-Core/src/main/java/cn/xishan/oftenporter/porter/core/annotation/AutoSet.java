package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;

import java.lang.annotation.*;

/**
 * 用于自动设置变量(任何访问类型，静态或非静态类型),包括父类的以及被设置的变量。
 * <br>
 * 从这些途径会触发AutoSet:
 * <pre>
 *     1.{@linkplain PortIn}
 *     2.{@linkplain AutoSetSeek}
 *     3.{@linkplain cn.xishan.oftenporter.porter.core.base.CheckPassable}
 *     4.{@linkplain cn.xishan.oftenporter.porter.core.base.StateListener}
 * </pre>
 * 当注解在{@linkplain Delivery}上时，{@linkplain AutoSet#value()}必须有，且为PName.
 * Created by https://github.com/CLovinr on 2016/9/8.
 * //TODO 循环设置的考虑
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface AutoSet
{
    public enum Range
    {
        /**
         * 表示使用针对所有context全局实例.若不存在，则会尝试反射创建，并加入到全局map中。
         * <br>
         * 见{@linkplain CommonMain#addGlobalAutoSet(String, Object)}
         */
        Global,
        /**
         * 表示使用当前context全局实例。若不存在，则会尝试反射创建，并加入到context的全局map中。
         * <br>
         * {@linkplain PorterConf#addContextAutoSet(String, Object)}
         */
        Context,
        /**
         * 表示新建一个独立的实例。
         */
        New
    }

    /**
     * 为""表示查找name为当前注解类的Class.getName,若不存在则会尝试(必须含有无参构造函数)反射创建.
     *
     * @return
     */
    String value() default "";

    /**
     * 做为{@linkplain #value()}的补充，当其不为默认值时，则使用classValue().getName()作为查找的name。
     *
     * @return
     */
    Class<?> classValue() default AutoSet.class;


    /**
     * 自动设置对象的查找范围。
     *
     * @return
     */
    Range range() default Range.Context;
}
