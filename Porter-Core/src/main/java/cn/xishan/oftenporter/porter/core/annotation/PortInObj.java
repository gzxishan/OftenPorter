package cn.xishan.oftenporter.porter.core.annotation;


import cn.xishan.oftenporter.porter.core.annotation.PortIn.PortStart;
import cn.xishan.oftenporter.porter.core.annotation.sth.Porter;
import cn.xishan.oftenporter.porter.core.annotation.sth.PorterOfFun;
import cn.xishan.oftenporter.porter.core.apt.AutoGen;
import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import cn.xishan.oftenporter.porter.core.base.ITypeParserOption;
import cn.xishan.oftenporter.porter.core.base.ParamDealt.FailedReason;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WObject;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * <pre>
 * 一、用于自动生成类或接口对象。对于该方式的参数类型绑定是全局的，例如有一个类A，其所有字段的{@linkplain ITypeParser}在一个地方被绑定了，那么在另一个地方则无需进行绑定。
 * 1.对于类，必须是非抽象类且含有无参构造函数。
 * 2.对于接口是以下形式:(默认为必需参数,需要用到Annotation Processor机制，见{@linkplain AutoGen})
 * 以get或is开头的变量名是去掉get或is再把第一个字符变为小写。
 * &#64;AutoGen
 * interface IDemo{
 *     String getName();
 *     boolean isOk();
 * }
 * 二、 <strong>参数的配置参数：</strong>{@linkplain Nece#value()}和{@linkplain UnNece#value()}支持{@linkplain ITypeParserOption}
 * 三、<strong>注意：</strong>1.对于此方式注解的绑定类，对应的field使用{@linkplain Parser}或{@linkplain Parser.parse}来手动绑定类型转换，可以注解在类或field上.
 * .如果field已经被绑定了转换类型，则此field（加了{@linkplain Nece}或{@linkplain UnNece}的）不会进行自动绑定。
 * 四、该注解加入的class类可以添加注解@{@linkplain InObjDealt}
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
     * 从类的@{@linkplain PortInObj.OnPorter}上获取对象绑定，且可以获取子类，但优先选择更亲的类。
     * <pre>
     *     如:
     *     class A{}
     *     class B extends A{};
     *     class FatherPorter{
     *          &#64;PortIn
     *          &#64;PortInObj({SomeClass.class})
     *          &#64;PortInObj.FromPorter({A.class})
     *          public   void fun(WObject wObject){
     *              SomeClass mSomeClass = wObject.finObject(0);
     *              A a = wObject.finObject(1);//对于ChildPorter,具体实例是B;对于Child2Porter,具体实例是A;
     *          }
     *     }
     *
     *     &#64;PortInObj({B.class})
     *     class ChildPorter extends FatherPorter{
     *
     *
     *     }
     *
     *      &#64;PortInObj({A.class,B.class})
     *     class Child2Porter extends FatherPorter{
     *
     *
     *     }
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @Inherited
    @Documented
    @interface FromPorter
    {
        Class<?>[] value() default {};
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Inherited
    @Documented
    @interface OnPorter
    {
        Class<?>[] value() default {};
    }

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
     * 每一个绑定对应一个此handle。
     *
     * @param <T>
     */
    public interface IInObjHandle<T>
    {
        /**
         * 初始化,在接口开始({@linkplain PortStart})前调用。
         *
         * @param option
         */
        public void init(String option, Method method);

        /**
         * 初始化,在接口开始({@linkplain PortStart})前调用。
         *
         * @param option
         */
        public void init(String option, Class<?> clazz);

        /**
         * 可以返回{@linkplain FailedReason}.
         *
         * @param porter
         * @param object
         * @return 返回最终对象
         */
        public Object deal(WObject wObject, Porter porter, @NotNull T object);

        /**
         * * 可以返回{@linkplain FailedReason}.
         *
         * @param fun
         * @param object
         * @return 返回最终对象
         */
        public Object deal(WObject wObject, PorterOfFun fun, @NotNull T object);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface InObjDealt
    {
        /**
         * 选项参数
         *
         * @return
         */
        String option() default "";

        /**
         * 处理类。
         *
         * @return
         */
        Class<? extends IInObjHandle> handle();
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
