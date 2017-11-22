package cn.xishan.oftenporter.oftendb.annotation;

import java.lang.annotation.*;

/**
 * @author Created by https://github.com/CLovinr on 2017/11/22.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface FederatedMysqlOption
{
    String tableName();

    /**
     * 包括端口，如localhost:3306
     *
     * @return
     */
    String host();

    String user();

    String password();

    String dbName();

    boolean dropIfExists();
}
