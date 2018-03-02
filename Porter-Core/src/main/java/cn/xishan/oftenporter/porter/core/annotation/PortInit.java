package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * 在框架启动成功后调用,调用完成后接口函数失效。
 * Created by chenyg on 2018-03-02.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
@Documented
public @interface PortInit
{
    /**
     * 数值越小越先执行
     * @return
     */
    int order()default Integer.MAX_VALUE;
}
