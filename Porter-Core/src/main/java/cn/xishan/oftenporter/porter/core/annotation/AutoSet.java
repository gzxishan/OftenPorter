package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetDealt;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.StateListener;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.sysset.TypeTo;
import org.slf4j.Logger;

import java.lang.annotation.*;

/**
 * 用于自动设置变量(任何访问类型，静态或非静态类型),包括父类的以及被设置的变量,支持泛型。
 * <br>
 * 从这些途径会触发AutoSet:
 * <pre>
 *     1.{@linkplain PortIn PortIn}
 *     2.{@linkplain AutoSetSeek AutoSetSeek}
 *     3.{@linkplain CheckPassable CheckPassable}
 *     4.{@linkplain StateListener StateListener}
 *     5.{@linkplain AutoSetDealt AutoSetDealt}和{@linkplain AutoSetGen AutoSetGen},这两个类的内部只能注入map中的、具有无参构造函数的或使用{@linkplain AutoSetGen AutoSetGen}生成的对象。
 * </pre>
 * <pre>
 * 内置对象:
 * 1.当注解在{@linkplain Delivery Delivery}上时，{@linkplain AutoSet#value()}表示PName，为空表示当前的.
 * 2.{@linkplain TypeTo TypeTo}
 * 3.{@linkplain PorterData PorterData}
 * 4.{@linkplain Logger Logger}
 * </pre>
 *
 * Created by https://github.com/CLovinr on 2016/9/8.
 * //TODO 循环设置的考虑
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface AutoSet
{


    /**
     * 被注解的类必须含有无参构造函数。
     *
     * @author Created by https://github.com/CLovinr on 2017/1/7.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Documented
    @interface AutoSetSeek
    {
    }

    /**
     * 注解在public的函数上(可以是静态函数，没有参数列表)，当对象的所有内部待设置的变量设置完成后调用被注解了的函数。
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @Documented
    @interface SetOk
    {

    }

    /**
     * 用于在混入接口类间传递变量，只对接口类变量有效,优先于{@linkplain AutoSet}注解。
     *
     * @author Created by https://github.com/CLovinr on 2017/2/7.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @Documented
    @interface AutoSetMixin
    {
        /**
         * 为""表示匹配名称为当前注解类的Class.getName，从被混入的类中查找有该注解的对应变量.
         *
         * @return
         */
        String value() default "";

        /**
         * 作为{@linkplain #value()}的补充，当{@linkplain #value()}不为""且其值不为默认值时，则使用classValue().getName()作为匹配名称。
         *
         * @return
         */
        Class<?> classValue() default AutoSetMixin.class;

        /**
         * 是否等待被设置,默认true。
         *
         * @return
         */
        boolean waitingForSet() default true;
    }

    /**
     * 用于传递混入对象,只包含声明的变量。
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @Documented
    @interface AutoSetThatOfMixin
    {

    }

    enum Range
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
     * 做为{@linkplain #value()}的补充，当{@linkplain #value()}不为""且其值不为默认值时，则使用classValue().getName()作为查找的name。
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

    String option() default "";

    /**
     * 是否允许注入的对象为空，默认为false。
     * @return
     */
    boolean nullAble()default false;

    /**
     * 对应的AutoSetDealt必须含有无参构造函数。
     * @return
     */
    Class<? extends AutoSetDealt> dealt() default AutoSetDealt.class;

    /**
     * 对应的AutoSetGen必须含有无参构造函数。
     * @return
     */
    Class<? extends AutoSetGen> gen() default AutoSetGen.class;

    /**
     * 对注入进行处理.
     * @author Created by https://github.com/CLovinr on 2017/1/7.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Documented
    @interface AutoSetDefaultDealt
    {
        /**
         * 默认选项，当{@linkplain AutoSet#option() AutoSet.option()}为空时有效。
         * @return
         */
        String option() default "";
        /**
         * 对应的AutoSetDealt必须含有无参构造函数。
         * @return
         */
        Class<? extends AutoSetDealt> dealt() default AutoSetDealt.class;

        /**
         * 对应的AutoSetGen必须含有无参构造函数。
         * @return
         */
        Class<? extends AutoSetGen> gen() default AutoSetGen.class;
    }
}
