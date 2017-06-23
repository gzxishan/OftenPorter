package cn.xishan.oftenporter.porter.core.annotation;

import cn.xishan.oftenporter.porter.core.base.WObject;

import java.lang.annotation.*;

/**
 * <pre>
 *     见{@linkplain PortInObj PortInObj},访问索引{@linkplain WObject#finObject(int) WObject.finObject(int)}位于之后
 * </pre>
 * Created by chenyg on 2017-06-13.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
@Documented
public @interface PortInObjBind
{
    /**
     * 绑定的名称
     *
     * @return
     */
    String[] value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Inherited
    @Documented
    @interface ObjList
    {
        Obj[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Inherited
    @Documented
    @interface Obj
    {
        /**
         * 与{@linkplain PortInObjBind#value()}进行联系。
         *
         * @return
         */
        String name();

        Class<?> clazz();
    }
}
