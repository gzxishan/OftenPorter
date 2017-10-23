package cn.xishan.oftenporter.oftendb.annotation;

import cn.xishan.oftenporter.oftendb.data.ConfigToDo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于单独自定义{@linkplain ConfigToDo}或表名
 *
 * @author Created by https://github.com/CLovinr on 2017/10/23.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CustomerToDo
{

    Class<? extends ConfigToDo> todo() default ConfigToDo.class;

    /**
     * 没有设置{@linkplain #todo()}的情况下有效
     * @return
     */
    String tableName() default "";
}
