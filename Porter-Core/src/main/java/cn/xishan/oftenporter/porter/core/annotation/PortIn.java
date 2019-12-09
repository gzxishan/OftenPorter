package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.advanced.AspectPosition;
import cn.xishan.oftenporter.porter.core.advanced.IPorter;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParserOption;
import cn.xishan.oftenporter.porter.core.annotation.param.Parse;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.advanced.IAnnotationConfigable;
import cn.xishan.oftenporter.porter.core.init.PorterConf;
import cn.xishan.oftenporter.porter.simple.DefaultUrlDecoder;
import cn.xishan.oftenporter.porter.simple.parsers.ObjectParser;
import cn.xishan.oftenporter.porter.simple.parsers.StringParser;

import java.lang.annotation.*;

/**
 * <ol>
 * <li>
 * 用于标记输入接口。若标记在函数上，函数(静态或非静态)且是public的;若标记在类上，访问类型可以是任意类型。
 * </li>
 * <li>
 * 支持的基本形参：{@linkplain OftenObject}
 * </li>
 * <li>
 * 输出方式见{@linkplain PortOut}
 * </li>
 * <li>
 * <strong>声明参数的配置选项：</strong>{@linkplain #nece()}和{@linkplain #unece()}支持{@linkplain ITypeParserOption}
 * </li>
 * <li>
 * 参数处理见:{@linkplain Parse},{@linkplain ITypeParser},以及{@linkplain StringParser}、{@linkplain ObjectParser}等
 * </li>
 * <li>
 * <strong> 配置参数</strong>见:{@linkplain IAnnotationConfigable},如@PortIn("${tiedName}")
 * </li>
 * <li>
 * 参数解析：{@linkplain DefaultUrlDecoder}
 * </li>
 * </ol>
 * <p>
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
@Documented
@AdvancedAnnotation(enableAdvancedAnnotation = true, enableCache = false)
public @interface PortIn
{

    /**
     * 注解在PortIn类上，用于为当前接口实例添加绑定名称(另见{@linkplain PorterConf#addContextAutoSet(String, Object)})、可通过@
     * {@linkplain AutoSet}获取.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Inherited
    @Documented
    @interface ContextSet
    {
        String value();
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
     * 在函数或类上，且大小不为0时有效。
     *
     * @return
     */
    String[] tieds() default {};

    /**
     * 注解在函数上有效，且当大小不为0时则使用其声明的请求方法。
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
     *     如果最终取值为{@linkplain TiedType#METHOD},则函数的绑定名失效，请求分别被分发到对应的{@linkplain PortMethod}绑定的函数上。
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
     * 是否允许{@linkplain MixinTo},对注解在类上的有效。
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

    /**
     * 使用指定的日志级别打印结果，取值：trace,debug,info,warn,error
     *
     * @return
     */
    String responseLoggerLevel() default "";
}
