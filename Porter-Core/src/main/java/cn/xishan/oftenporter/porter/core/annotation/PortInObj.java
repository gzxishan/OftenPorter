package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.apt.AutoGen;
import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import cn.xishan.oftenporter.porter.core.base.PortMethod;

import java.lang.annotation.*;

/**
 * <pre>
 * 用于自动生成类或接口对象。对于该方式的参数类型绑定是全局的，例如有一个类A，其所有字段的{@linkplain ITypeParser}在一个地方被绑定了，那么在另一个地方则无需进行绑定。
 * 1.对于类，必须是非抽象类且含有无参构造函数。
 * 2.对于接口是以下形式:(默认为必需参数,需要用到Annotation Processor机制，见{@linkplain AutoGen})
 * 以get或is开头的变量名是去掉get或is再把第一个字符变为小写。
 * &#64;AutoGen
 * interface IDemo{
 *     String getName();
 *     boolean isOk();
 * }
 * </pre>
 * <p>
 * <strong>注意：</strong>对于此方式注解的绑定类，对应的field使用{@linkplain Parser}或{@linkplain Parser.parse}来手动绑定类型转换，可以注解在类或field上.
 * </p>
 * Created by https://github.com/CLovinr on 2016/9/7.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Documented
public @interface PortInObj
{
    /**
     * 对私有字段也有效。
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @Inherited
    @Documented
    @interface Nece
    {
        /**
         * 为""表示使用变量的名称。
         *
         * @return
         */
        String value() default "";

        /**
         * <pre>
         *     若当前请求在{@linkplain #forMethods()}、{@linkplain #forClassTieds()} ()}或{@linkplain #forFunTieds()} ()}中,或以上都为空时。
         *     1.为true时：注解的变量任然为必需参数。
         *     2.为false时：注解的变量变为非必需参数。
         * </pre>
         *
         * @return 默认为true。
         */
        boolean forNece() default true;

        PortMethod[] forMethods() default {};

        String[] forClassTieds() default {};

        String[] forFunTieds() default {};
    }

    /**
     * 对私有字段也有效。
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @Inherited
    @Documented
    @interface UnNece
    {
        /**
         * 为""表示使用变量的名称。
         *
         * @return
         */
        String value() default "";
    }


    /**
     * 类或接口。对应的类或接口可以使用{@linkplain Parser}或{@linkplain Parser.parse}来绑定类型转换;
     * 对应的变量或接口函数可以用{@linkplain Parser.parse}来绑定类型转换。
     * <br>
     *
     * @return
     */
    Class<?>[] value() default {};
}
