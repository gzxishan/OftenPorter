package cn.xishan.oftenporter.oftendb.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * 说明：只支持在mapper文件中调用，通过$[key]来引用
 * </p>
 * <ol>
 * <li>
 * &#60;!--$classpath:path[!JsonString]--&#62;表示导入包内path资源内容,相对于对应的Dao的类路径(utf8编码),会覆盖之前的同名参数
 * </li>
 * <li>
 * &#60;!--$file:path[!JsonString]--&#62;表示导入path文件内容(utf8编码),会覆盖之前的同名参数
 * </li>
 * <li>
 * &#60;!--$json:JsonString--&#62;表示导入配置,会覆盖之前的同名参数
 * </li>
 * <li>
 * value为json格式的字符串{key:value}.
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
    String[] value();
}
