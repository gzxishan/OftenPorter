package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.base.CheckPassable;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.TiedType;

import java.lang.annotation.*;

/**
 * 1.用于标记输入接口。若标记在函数上，要求函数(静态或非静态)必须是public的;若标记在类上，则访问类型可以是任意类型。
 * <br>
 * 2.返回值见{@linkplain PortOut}
 * Created by https://github.com/CLovinr on 2016/7/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
@Documented
public @interface PortIn
{

    /**
     * 加在接口类上，表示只用于混入。
     *
     * @author Created by https://github.com/CLovinr on 2017/3/4.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Inherited
    @Documented
    @interface MinxinOnly
    {
    }


    /**
     * 用于标记函数(public)，在销毁时调用。
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @Inherited
    @Documented
    @interface PortDestroy
    {
        /**
         * 在接口类中被调用的顺序。
         *
         * @return
         */
        int order() default 0;
    }


    /**
     * 用于标记函数(public)，启动时调用。
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @Inherited
    @Documented
    @interface PortStart
    {
        /**
         * 在接口类中被调用的顺序。
         *
         * @return
         */
        int order() default 0;
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
     *     1.如果注解的是类，则绑定名是类名,并且会去掉末尾的"WPort"或"Porter"。
     *       如："Hello"="Hello","HelloPorter"="Hello"
     *     2.如果注解的是函数，则绑定名是函数名。
     * </pre>
     */
    String tied() default "";

    /**
     * 必须参数列表.
     */
    String[] nece() default {};

    /**
     * 非必须参数列表.
     */
    String[] unnece() default {};

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
     * 是否忽略类型转换。默认为false。当为true时，将跳过{@linkplain cn.xishan.oftenporter.porter.core.base.ITypeParser}转换。
     *
     * @return
     */
    boolean ignoreTypeParser() default false;

    /**
     * 设置检测类型(对类或函数阶段有效，但对混入接口无效。)。会依此进行检测，有一个不通过则表示访问不通过。对应的类必须有无参构造函数。
     */
    Class<? extends CheckPassable>[] checks() default {};

    /**
     * 设置检测类型(对类和函数阶段有效，且对混入接口也有效)。会依此进行检测，有一个不通过则表示访问不通过。对应的类必须有无参构造函数。
     * <br>
     * <strong>注意：</strong>只对类有效，且在{@linkplain #checks()}之前被调用。
     */
    Class<? extends CheckPassable>[] checksForWholeClass() default {};

    /**
     * <pre>
     *     如果最终取值为{@linkplain TiedType#REST},则函数的绑定名失效，请求分别被分发到对应的{@linkplain PortMethod}绑定的函数上。
     * </pre>
     */
    TiedType tiedType() default TiedType.Default;

}
