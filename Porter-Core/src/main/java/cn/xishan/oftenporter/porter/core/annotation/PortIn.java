package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.advanced.AspectPosition;
import cn.xishan.oftenporter.porter.core.advanced.IPorter;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParserOption;
import cn.xishan.oftenporter.porter.core.annotation.param.Parse;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetDealt;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.advanced.IAnnotationConfigable;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.simple.parsers.ObjectParser;
import cn.xishan.oftenporter.porter.simple.parsers.StringParser;

import java.lang.annotation.*;

/**
 * <ol>
 * <li>
 * 用于标记输入接口。若标记在函数上，函数(静态或非静态)且是public的;若标记在类上，访问类型可以是任意类型。
 * </li>
 * <li>
 * 返回值见{@linkplain PortOut}
 * </li>
 * <li>
 * <strong>参数的参数：</strong>{@linkplain #nece()}和{@linkplain #unece()}支持{@linkplain ITypeParserOption}
 * </li>
 * <li>
 * 参数处理见:{@linkplain Parse},{@linkplain ITypeParser},以及{@linkplain StringParser}、{@linkplain ObjectParser}等
 * </li>
 * <li>
 * <strong> 配置参数</strong>见:{@linkplain IAnnotationConfigable},如@PortIn("${tiedName}")
 * </li>
 * </ol>
 * <p>
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
@Documented
@Advancable(enableAdvancedAnnotation = true)
public @interface PortIn
{

    /**
     * 注解在接口类上，用于添加变量(另见{@linkplain PorterConf#addContextAutoSet(String, Object)})、可通过@{@linkplain AutoSet}获取.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Documented
    @interface ContextSet
    {
        String value();
    }

    /**
     * 用于标记函数(public)，在销毁时调用。
     * <pre>
     *     1.可用于{@linkplain PortIn}、{@linkplain AutoSet}、{@linkplain AutoSetGen}、{@linkplain AutoSetDealt}类中
     *     2.具有继承性
     *     3.执行顺序：其他对象中的---&gt;{@linkplain PortIn}中的
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @Inherited
    @Documented
    @interface PortDestroy
    {
        /**
         * 在接口类或全局中被调用的顺序,数值越小越先执行,或者在飞porter接口中的顺序.
         *
         * @return
         */
        int order() default 0;
    }


    /**
     * 用于标记函数(public)，启动时调用。
     * <pre>
     * 1.函数可以无形参，或者有一个形参WObject(其请求类绑定名为当前接口类的)
     * 2.可用于{@linkplain PortIn}、{@linkplain AutoSet}、{@linkplain AutoSetGen}、{@linkplain AutoSetDealt}类中
     * 3.具有继承性
     * 4.执行顺序：其他对象中的---&gt;{@linkplain PortIn}中的
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @Inherited
    @Documented
    @interface PortStart
    {
        /**
         * 在接口类或全局中被调用的顺序,数值越小越先执行,或者在飞porter接口中的顺序.
         *
         * @return
         */
        int order() default 10000;
    }

    /**
     * 同{@linkplain #tied()},当不为""时，则覆盖{@linkplain #tied()}.
     *
     * @return
     */
    String value() default "";

    /**
     * 绑定的名字.
     * <pre>
     *     合法的字符是:字母、数字、"-"、"_"、"%"、"."、"$"、"&"、"="
     *     为""的情况下:
     *     1.如果注解的是类，则绑定名是类名,并且会去掉末尾的"WPort"或"Porter"。(见{@linkplain IPorter})
     *       如："Hello"="Hello","HelloPorter"="Hello"
     *     2.如果注解的是函数，则绑定名是函数名。
     *     3.不能出现'/'
     * </pre>
     */
    String tied() default "";

    /**
     * 注解在函数上，且大小不为0时有效。
     *
     * @return
     */
    String[] tieds() default {};

    /**
     * 大小不为0时有效。
     *
     * @return
     */
    PortMethod[] methods() default {};

    /**
     * 必须参数列表.
     */
    String[] nece() default {};

    /**
     * 非必须参数列表.
     */
    String[] unece() default {};

    String[] inner() default {};

    /**
     * 接口的方法类型。
     * <pre>
     *     1.当类和函数都是{@linkplain PortMethod#DEFAULT}类型时，则为GET；
     *     2.当类和函数只有一个是DEFAULT类型时，则以非DEFAULT的那个为准；
     *     3.当类和函数都不为DEFAULT类型时，则以函数的类型为准。
     * </pre>
     */
    PortMethod method() default PortMethod.DEFAULT;

    /**
     * 是否忽略所在类（或函数）的类型转换，默认为false。当为true时，将跳过{@linkplain ITypeParser}转换。
     *
     * @return
     */
    boolean ignoreTypeParser() default false;

    /**
     * 忽略指定绑定名的接口方法，对类有效。
     *
     * @return
     */
    String[] ignoredFunTieds() default {};

    /**
     * <ol>
     * <li>
     * 注解在类上时，对应的阶段有{@linkplain DuringType#BEFORE_CLASS},{{@linkplain DuringType#ON_CLASS},
     * {@linkplain DuringType#BEFORE_METHOD},
     * {@linkplain DuringType#ON_METHOD},{@linkplain DuringType#ON_METHOD_EXCEPTION}
     * </li>
     * <li>
     * 注解在函数上时，对应的阶段有{@linkplain DuringType#BEFORE_METHOD},
     * * {@linkplain DuringType#ON_METHOD},{@linkplain DuringType#ON_METHOD_EXCEPTION}
     * </li>
     * </ol>
     * <strong>注意：</strong>在{@linkplain #checksForWholeClass()}之后添加,对混入接口无效。
     */
    Class<? extends CheckPassable>[] checks() default {};

    /**
     * 只对类上的注解有效，对应的阶段有{@linkplain DuringType#BEFORE_CLASS},{@linkplain DuringType#ON_CLASS},
     * {@linkplain DuringType#BEFORE_METHOD},
     * {@linkplain DuringType#ON_METHOD},{@linkplain DuringType#ON_METHOD_EXCEPTION}
     * <br>
     * <strong>注意：</strong>在{@linkplain #checks()}之前添加,对混入接口也有效。
     */
    Class<? extends CheckPassable>[] checksForWholeClass() default {};

    /**
     * <pre>
     *     如果最终取值为{@linkplain TiedType#REST},则函数的绑定名失效，请求分别被分发到对应的{@linkplain PortMethod}绑定的函数上。
     * </pre>
     */
    TiedType tiedType() default TiedType.DEFAULT;


    PortFunType portFunType() default PortFunType.DEFAULT;

    /**
     * 对接口函数有效,设置类上的被{@linkplain AspectOperationOfPortIn}修饰的注解如何添加到接口函数上，默认为{@linkplain AspectPosition#BEFORE}
     *
     * @return
     */
    AspectPosition aspectOfClassPosition() default AspectPosition.BEFORE;

    /**
     * 是否允许{@linkplain MixinTo}
     *
     * @return
     */
    boolean enableMixinTo() default true;

    /**
     * 对类有效，设置之后，通过该key来{@linkplain AutoSet}，从而得到当前的接口实例。
     *
     * @return
     */
    Class<?> toPorterKey() default PortIn.class;

}
