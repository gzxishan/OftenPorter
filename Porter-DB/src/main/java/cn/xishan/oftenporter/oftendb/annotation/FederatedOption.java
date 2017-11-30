package cn.xishan.oftenporter.oftendb.annotation;

import java.lang.annotation.*;

/**
 * <pre>
 *     1.用于自动触发。
 *     2.当注解在类上时，可以提供公共信息.dropIfExists都为-1表示true。
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2017/11/22.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface FederatedOption
{
    String tableName() default "";

    String driverClass() default "";

    String jdbcUrl() default "";

    String connectionUrl() default "";

    /**
     * -1表示默认，0表示false，非0表示true
     * @return
     */
    int dropIfExists() default -1;

    /**
     * 操作失败的尝试次数
     * @return
     */
    int tryCount()default 0;
}
