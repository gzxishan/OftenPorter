package cn.xishan.oftenporter.servlet;


import java.lang.annotation.*;

/**
 * 有该注解的对象，只能保存在本服务器。
 * @author Created by https://github.com/CLovinr on 2018/8/19.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Inherited
public @interface LocalSessionValue
{


}
