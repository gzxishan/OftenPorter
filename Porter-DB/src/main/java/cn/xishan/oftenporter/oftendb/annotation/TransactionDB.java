package cn.xishan.oftenporter.oftendb.annotation;

import cn.xishan.oftenporter.oftendb.annotation.tx.Isolation;
import cn.xishan.oftenporter.oftendb.annotation.tx.Readonly;
import cn.xishan.oftenporter.oftendb.db.sql.TransactionDBHandle;
import cn.xishan.oftenporter.oftendb.mybatis.MyBatisOption;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;

import java.lang.annotation.*;

/**
 * jdbc事务,全局配置enableTransactionJDBC:true|false
 *
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.TYPE, ElementType.METHOD
})
@Documented
@AspectOperationOfNormal(handle = TransactionDBHandle.class)
public @interface TransactionDB
{

    /**
     * 事设置务隔离级别
     *
     * @return
     */
    Isolation level() default Isolation.DEFAULT;

    /**
     * 是否只读，默认false,如果有一个为false则所有的为false。
     *
     * @return
     */
    Readonly readonly() default Readonly.DEFAULT;

    /**
     * 是否设置保存点。
     *
     * @return
     */
    boolean setSavePoint() default false;

    /**
     * 数据源名称，另见{@linkplain MyBatisOption#source}
     *
     * @return
     */
    String dbSource() default MyBatisOption.DEFAULT_SOURCE;


    String type() default "mybatis";

    /**
     * 用于设置{@linkplain java.sql.Statement#setQueryTimeout(int)}，单位是秒,默认为-1表示不设置，多个的情况选择最小值。
     *
     * @return
     */
    int queryTimeoutSeconds() default -1;

}
