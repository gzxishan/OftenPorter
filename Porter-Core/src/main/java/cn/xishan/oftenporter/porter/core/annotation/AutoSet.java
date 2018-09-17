package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.advanced.ResponseHandle;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortDestroy;
import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortStart;
import cn.xishan.oftenporter.porter.core.annotation.param.BindEntityDealt;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetDealt;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;
import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.StateListener;
import cn.xishan.oftenporter.porter.core.init.CommonMain;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.core.pbridge.Delivery;
import cn.xishan.oftenporter.porter.core.sysset.PorterData;
import cn.xishan.oftenporter.porter.core.sysset.SyncNotInnerPorter;
import cn.xishan.oftenporter.porter.core.sysset.SyncPorter;
import cn.xishan.oftenporter.porter.core.sysset.TypeTo;
import org.slf4j.Logger;

import javax.annotation.Resource;
import java.lang.annotation.*;

/**
 * 用于自动设置变量(任何访问类型，静态或非静态类型),包括父类的以及被设置的变量,支持泛型。属性设置见{@linkplain Property}。
 * <pre>
 *     注意:
 *     1.递归扫描时会忽略对所有以"java."开头的类。
 *     2.对于不为null的成员，会忽略变量的设置，但会进行递归扫描。
 *     3.若被设置的变量不为null，则会忽略变量获取、递归设置，但会执行{@linkplain AutoSetDealt}、{@linkplain SetOk}、{@linkplain #notNullPut()}。
 *     4.含有@{@linkplain Property}的变量也会被设置。
 *     5.注解在函数上时，形参变量需要用@{@linkplain Property}来获取配置,且含有该注解的类变量先被设置。
 * </pre>
 * <hr>
 * <p>
 * 从这些途径会触发AutoSet:
 * <ol>
 * <li>
 * 被AutoSet的对象
 * </li>
 * <li>
 * {@linkplain PortIn PortIn}
 * </li>
 * <li>
 * {@linkplain AutoSetSeek AutoSetSeek}
 * </li>
 * <li>
 * {@linkplain CheckPassable CheckPassable}
 * </li>
 * <li>
 * {@linkplain StateListener StateListener}
 * </li>
 * <li>
 * {@linkplain AutoSetDealt AutoSetDealt}和{@linkplain AutoSetGen AutoSetGen},     这两个类的内部只能注入map
 * 中的、具有无参构造函数的或使用{@linkplain AutoSetGen AutoSetGen}生成的对象。
 * </li>
 * <li>
 * {@linkplain AspectOperationOfPortIn.Handle}
 * </li>
 * <li>
 * {@linkplain BindEntityDealt}
 * </li>
 * <li>
 * {@linkplain AspectOperationOfNormal.Handle}
 * </li>
 * <li>
 * {@linkplain ResponseHandle}
 * </li>
 * </ol>
 * </p>
 * <hr>
 * 不会触发{@linkplain SetOk}:
 * <ol>
 * <li>
 * {@linkplain AutoSetDealt AutoSetDealt}和{@linkplain AutoSetGen AutoSetGen}(但有{@linkplain PortStart}与
 * {@linkplain PortDestroy})
 * </li>
 * </ol>
 * <pre>
 * 内置对象:
 * 1.当注解在{@linkplain Delivery Delivery}上时，{@linkplain AutoSet#value()}表示PName，为空表示当前的.
 * 2.{@linkplain TypeTo TypeTo}
 * 3.{@linkplain PorterData PorterData}
 * 4.{@linkplain Logger Logger}
 * 5.{@linkplain SyncPorter SyncPorter}
 * 6.{@linkplain SyncNotInnerPorter SyncNotInnerPorter}
 * </pre>
 * <p>
 * <hr>
 * 注意，支持@{@linkplain Resource}注解(但不会对相应变量进行递归设置)，映射关系如下：
 * <ol>
 * <li>
 * {@linkplain #nullAble()}:true
 * </li>
 * <li>
 * {@linkplain #notNullPut()}:true
 * </li>
 * <li>
 * {@linkplain #value()}:{@linkplain Resource#name()}
 * </li>
 * <li>
 * {@linkplain #classValue()}:{@linkplain Resource#type()}
 * </li>
 * <li>
 * {@linkplain #range()}:
 * <ol>
 * <li>
 * {@linkplain Range#New}:{@linkplain Resource#shareable()}为false时
 * </li>
 * <li>
 * {@linkplain Range#Global}:{@linkplain Resource#authenticationType()} ()
 * }为{@linkplain Resource.AuthenticationType#CONTAINER}时
 * </li>
 * <li>
 * {@linkplain Range#Context}:{@linkplain Resource#authenticationType()} ()
 * }为{@linkplain Resource.AuthenticationType#APPLICATION}时
 * </li>
 * </ol>
 * </li>
 * </ol>
 * </p>
 * <p>
 * Created by https://github.com/CLovinr on 2016/9/8.
 * //TODO 循环设置的考虑
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface AutoSet
{


    /**
     * <pre>
     * 注解在函数上(可以是静态函数，没有参数列表)，当对象的所有内部待设置的变量设置完成后调用被注解了的函数。
     * <strong>注意：</strong>1.只有对象里含有{@linkplain AutoSet}注解的才会触发注解了。
     *        2.函数可以无形参，或者有一个形参WObject。
     *        3.在{@linkplain PortStart PortStart}之前调用。
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @Documented
    @interface SetOk
    {
        /**
         * 全局优先级,数值越大越先执行，同一优先级的执行顺序并不保证。
         *
         * @return
         */
        int priority() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @Documented
    @interface Put
    {
        Range range() default Range.Context;

        String name() default "";
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
         * 表示使用当前context全局实例。若不存在，会先尝试查找PortIn接口实例，最后会尝试反射创建、并加入到context的全局map中。
         * <br>
         * 另见{@linkplain PorterConf#addContextAutoSet(String, Object)}
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

    /**
     * 是否在被设置的变量不为null、且{@linkplain #range()}为{@linkplain Range#Global}或{@linkplain Range#Context}时，
     * 存储变量以供其他地方进行变量的自动设置。
     *
     * @return 默认为true。
     */
    boolean notNullPut() default true;

    String option() default "";

    /**
     * 是否允许注入的对象为空，默认为false。
     *
     * @return
     */
    boolean nullAble() default false;

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
