package cn.xishan.oftenporter.oftendb.annotation;

import cn.xishan.oftenporter.oftendb.mybatis.MyBatisOption;

import java.lang.annotation.*;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface MyBatisField
{
    Class<?> value()default Object.class;

    /**
     * 指定数据源，见{@linkplain MyBatisOption#source}
     *
     * @return
     */
    String source() default MyBatisOption.DEFAULT_SOURCE;
}
