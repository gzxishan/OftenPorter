package cn.xishan.oftenporter.oftendb.annotation;

import java.lang.annotation.*;

/**
 * @author Created by https://github.com/CLovinr on 2018-11-09.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface ExceptColumns
{
    /**
     * 排除的字段,如remark。
     *
     * @return
     */
    String[] fields() default {};

    /**
     * select-part排除的字段
     * @return
     */
    String[] selectPart()default {};

    /**
     * insert-part排除的字段
     * @return
     */
    String[] insertPart()default {};

    /**
     * update-part排除的字段
     * @return
     */
    String[] updatePart()default {};

    /**
     * 是否启用xml里except配置的排除项，默认为true。
     *
     * @return
     */
    boolean enableXmlConfiged() default true;
}
