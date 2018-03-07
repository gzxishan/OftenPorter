package cn.xishan.oftenporter.oftendb.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * 说明：只支持在mapper文件中调用
 * </p>
 * <ol>
 * <li>
 * &#60;!--$classpath:path[!json]--&#62;表示导入包内path资源内容,相对于对应的Dao的类路径(utf8编码),会覆盖之前的同名参数
 * </li>
 * <li>
 * &#60;!--$file:path[!json]--&#62;表示导入path文件内容(utf8编码),会覆盖之前的同名参数
 * </li>
 * <li>
 * json格式的字符串{key:value}，在mybatis动态sql中通过${key}来引用.
 * </li>
 * </ol>
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
