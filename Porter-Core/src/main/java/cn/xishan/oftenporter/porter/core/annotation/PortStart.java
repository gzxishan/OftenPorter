package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 用于标记函数(public)，启动时调用。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
@Documented
public @interface PortStart
{
    /**
     * 在接口类中被调用的顺序。
     * @return
     */
    int order()default 0;
}
