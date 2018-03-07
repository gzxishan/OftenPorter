package cn.xishan.oftenporter.oftendb.annotation;

import java.lang.annotation.*;

/**
 * json格式的字符串{key:value}，在mybatis动态sql中通过${key}来引用,且只支持在mapper文件中调用.
 * <p>注解在Dao上</p>
 * Created by chenyg on 2018-03-06.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface MyBatisParams
{
    String value();
}
