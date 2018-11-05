package cn.xishan.oftenporter.oftendb.annotation;

import cn.xishan.oftenporter.oftendb.util.TableOption;

import java.lang.annotation.*;

/**
 * 对查询条件进行过滤
 * Created by chenyg on 2017-04-14.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Documented
public @interface TableOptionFilter
{
    /**
     * 是否忽略空的包含列表，默认false。
     *
     * @return
     */
    boolean disableEmptyContains() default false;

    String[] queryContains() default {};

    String[] queryArrayContains() default {};

    String[] settingsContains() default {};

    String[] orderContains() default {};

    /**
     * 用于替换key,格式"key=newKey"
     *
     * @return
     */
    String[] replaceKey() default {};

    /**
     * 通过{@linkplain TableOption#setDefaultHandle(TableOption.IHandle)}设置全局默认的。
     *
     * @return
     */
    Class<? extends TableOption.IHandle> handle() default TableOption.IHandle.class;
}
