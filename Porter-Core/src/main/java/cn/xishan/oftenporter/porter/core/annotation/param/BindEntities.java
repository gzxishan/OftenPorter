package cn.xishan.oftenporter.porter.core.annotation.param;

import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.ITypeParser;
import cn.xishan.oftenporter.porter.core.base.ITypeParserOption;

import java.lang.annotation.*;

/**
 * <pre>
 * 一、对于该方式的参数类型绑定是全局的，例如有一个类A，其所有字段的{@linkplain ITypeParser}在一个地方被绑定了，那么在另一个地方则无需进行绑定。
 * 1.注解在{@linkplain PortIn}类或函数上。
 * 2.对于类，必须是非抽象类且含有无参构造函数。
 * 二、 <strong>参数的配置参数：</strong>{@linkplain Nece#value()}和{@linkplain Unece#value()}支持{@linkplain ITypeParserOption}
 * 三、<strong>注意：</strong>1.对于此方式注解的绑定类，对应的field使用{@linkplain Parse}来手动绑定类型转换，可以注解在类或field上.
 * .如果field已经被绑定了转换类型，则此field（加了{@linkplain Nece}或{@linkplain Unece}的）不会进行自动绑定。
 * 四、该注解加入的实体类可以添加注解@{@linkplain BindEntityDealt},可以对结果进行处理
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2018/6/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Documented
public @interface BindEntities
{
    /**
     * 类或接口。对应的类可以使用{@linkplain Parse}来绑定类型转换;
     * 对应的变量或接口函数可以用{@linkplain Parse}来绑定类型转换。
     * <br>
     *
     * @return
     */
    Class<?>[] value() default {};
}
