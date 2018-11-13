package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetDealt;
import cn.xishan.oftenporter.porter.core.annotation.sth.AutoSetGen;

import java.lang.annotation.*;

/**
 * 用于标记函数(public)，启动时调用。
 * <pre>
 * 1.函数可添加的参数：WObject(其请求类绑定名为当前接口类的)，{@linkplain IConfigData}
 * 2.可用于{@linkplain PortIn}、{@linkplain AutoSet}、{@linkplain AutoSetGen}、{@linkplain AutoSetDealt}类中
 * 3.具有继承性
 * 4.执行顺序：其他对象中的---&gt;{@linkplain PortIn}中的
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
@Documented
public @interface PortStart
{
    /**
     * 在接口类或全局中被调用的顺序,数值越小越先执行,或者在飞porter接口中的顺序.
     *
     * @return
     */
    int order() default 10000;
}
