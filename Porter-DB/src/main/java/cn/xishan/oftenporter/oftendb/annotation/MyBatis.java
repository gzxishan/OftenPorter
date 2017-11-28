package cn.xishan.oftenporter.oftendb.annotation;

import cn.xishan.oftenporter.oftendb.mybatis.MyBatisOption;

import java.lang.annotation.*;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface MyBatis
{
    enum Type
    {
        RESOURCES, URL
    }

    /**
     * 相对于{@linkplain MyBatisOption#rootDir}
     *
     * @return
     */
    String dir() default "";

    /**
     * 为空时，表示"当前类名.xml"
     *
     * @return
     */
    String name() default "";

    Type type() default Type.RESOURCES;
}
