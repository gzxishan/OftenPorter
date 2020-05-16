package cn.xishan.oftenporter.servlet.render.mapping;

import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfPortIn;
import cn.xishan.oftenporter.porter.core.annotation.PortIn;
import cn.xishan.oftenporter.porter.core.base.PortMethod;

import java.lang.annotation.*;

/**
 * 注解在{@linkplain PortIn}接口函数上，用于直接绑定servlet-path,需要自行处理响应。
 *
 * @author Created by https://github.com/CLovinr on 2020/5/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@AspectOperationOfPortIn(handle = PathMappingHandle.class)
public @interface PathMapping
{
    /**
     * 实际访问的页面路径（规则同servlet）。
     *
     * @return
     */
    String[] path();

    PortMethod[] method() default {PortMethod.GET};

    /**
     * 对于同一个请求被多次匹配的情况，数值更小的会被执行。
     *
     * @return
     */
    int order() default 0;

    boolean enable() default true;
}
