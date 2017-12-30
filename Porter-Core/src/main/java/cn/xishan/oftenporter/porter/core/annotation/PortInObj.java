package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.apt.AutoGen;
import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import cn.xishan.oftenporter.porter.core.base.ITypeParserOption;
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
 * 3. <strong>参数的配置参数：</strong>{@linkplain Nece#value()}和{@linkplain UnNece#value()}支持{@linkplain ITypeParserOption}
 * </pre>
 * <pre>
 * <strong>注意：</strong>1.对于此方式注解的绑定类，对应的field使用{@linkplain Parser}或{@linkplain Parser.parse}来手动绑定类型转换，可以注解在类或field上.
 * 2.如果field已经被绑定了转换类型，则此field（加了{@linkplain Nece}或{@linkplain UnNece}的）不会进行自动绑定。
 * </pre>
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
         * 转换为非必需参数。
         * <pre>
         *     若返回true且当前请求在{@linkplain #forMethods()}、{@linkplain #forClassTieds()} ()}或{@linkplain #forFunTieds()}
         *     ()}中时:
         *      注解的变量变为非必需参数。
         * </pre>
         *
         * @return 默认为false。
         */
        boolean toUnece() default false;

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
     * 用于添加。
     * Created by https://github.com/CLovinr on 2016/9/9.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface JsonField
    {
        /**
         * 默认为"",表示使用变量名;否则使用该值。
         */
        String value() default "";

        boolean filterNullAndEmpty() default false;
    }


    /**
     * 用于对象转json时。
     * Created by https://github.com/CLovinr on 2016/9/9.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface JsonObj
    {
        /**
         * 默认为"",表示使用变量名;否则使用该值。
         */
        String value() default "";

        boolean filterNullAndEmpty() default false;

        /**
         * 请求时，是否设置内部变量
         *
         * @return
         */
        boolean willSetForRequest() default false;

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
