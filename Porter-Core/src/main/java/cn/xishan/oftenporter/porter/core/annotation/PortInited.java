package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.advanced.IConfigData;
import cn.xishan.oftenporter.porter.core.base.OftenObject;

import java.lang.annotation.*;

/**
 * 在{@linkplain PortInit}后执行,对所有porter和被设置的对象都有效。
 * <pre>
 *     函数可添加的参数：{@linkplain OftenObject}(其请求类绑定名为当前接口类的)，{@linkplain IConfigData}
 * </pre>
 * Created by chenyg on 2018-03-02.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
@Documented
@AdvancedAnnotation(enableCache = false)
public @interface PortInited
{
    /**
     * 数值越小越先执行
     *
     * @return
     */
    int order() default Integer.MAX_VALUE;
}
