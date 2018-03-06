package cn.xishan.oftenporter.oftendb.annotation;

import cn.xishan.oftenporter.oftendb.mybatis.MyBatisOption;

import java.lang.annotation.*;

/**
 * <p>注解在Dao上</p>
 *
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

    /**
     * 为空时，是否注册别名由{@linkplain MyBatisOption#autoRegisterAlias};没有该注解时，不会自动注册别名。
     *
     * @return
     */
    String daoAlias() default "";

    /**
     * 为空时且{@linkplain #entityClass()}被设置了时，是否注册别名由{@linkplain MyBatisOption#autoRegisterAlias};没有该注解时，不会自动注册别名。
     *
     * @return
     */
    String entityAlias() default "";

    Class<?> entityClass() default MyBatis.class;

}
