package cn.xishan.oftenporter.oftendb.annotation;

import cn.xishan.oftenporter.oftendb.mybatis.MyBatisOption;
import cn.xishan.oftenporter.porter.core.annotation.deal.AnnoUtil;

import java.lang.annotation.*;

/**
 * <p>注解在mapper的java接口上.</p>
 * <p>见：{@linkplain MyBatisParams}</p>
 *
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface MyBatisMapper
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
     * 为空时，是否注册别名由{@linkplain MyBatisOption#autoRegisterAlias}决定;没有该注解时，不会自动注册别名。
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

    /**
     * 从被注解的当前interface上获取实际的实体类,见{@linkplain AnnoUtil.Advanced#getDirectGenericRealTypeAt(Class, int)}
     *
     * @return
     */
    int entityClassFromGenericTypeAt() default -1;

    /**
     * 获取泛型上的子类型作为实体,见{@linkplain AnnoUtil.Advanced#getDirectGenericRealTypeBySuperType(Class, Class)}
     *
     * @return
     */
    Class<?> entityClassFromGenericTypeBySuperType() default MyBatisMapper.class;

    /**
     * 优先于{@linkplain #entityClassFromGenericTypeAt()}
     *
     * @return
     */
    Class<?> entityClass() default MyBatisMapper.class;

    /**
     * json格式，会被@{@linkplain MyBatisParams}覆盖。
     *
     * @return
     */
    String[] params() default "";
}
