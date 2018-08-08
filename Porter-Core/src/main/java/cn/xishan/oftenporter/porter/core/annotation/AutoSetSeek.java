package cn.xishan.oftenporter.porter.core.annotation;

import java.lang.annotation.*;

/**
 * <ol>
 * <li>注解在类上：被注解的类必须含有无参构造函数。</li>
 * <li>注解在成员变量上：在变量不为null时，对变量内部属性进行AutoSet处理</li>
 * </ol>
 *
 * @author Created by https://github.com/CLovinr on 2017/1/7.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Documented
public @interface AutoSetSeek
{
}
