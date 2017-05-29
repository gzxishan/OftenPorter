package cn.xishan.oftenporter.oftendb.annotation;

import cn.xishan.oftenporter.oftendb.jbatis.JDao;

import java.lang.annotation.*;

/**
 * <pre>
 *  注解在{@linkplain JDao JDao}上。
 * 1.相对于当前Unit的路径,支持:../（上一级目录）,./（当前目录）,/（从根目录开始）
 * </pre>
 * Created by chenyg on 2017-05-10.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface JDaoPath
{
    String value()default "";
}
