package cn.xishan.oftenporter.oftendb.annotation;

import cn.xishan.oftenporter.oftendb.annotation.tx.Isolation;
import cn.xishan.oftenporter.oftendb.db.sql.TransactionJDBCHandle;
import cn.xishan.oftenporter.oftendb.mybatis.MyBatisOption;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;

import java.lang.annotation.*;
import java.sql.Connection;

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
@Inherited
@AspectOperationOfNormal(handle = TransactionJDBCHandle.class)
public @interface TransactionJDBC
{

    /**
     * 事设置务隔离级别
     *
     * @return
     */
    Isolation level() default Isolation.DEFAULT;

    /**
     * 是否只读，默认false
     *
     * @return
     */
    boolean readonly() default false;

    /**
     * 数据源名称，另见{@linkplain MyBatisOption#source}
     *
     * @return
     */
    String dbSource() default MyBatisOption.DEFAULT_SOURCE;


    String type() default "mybatis";

    /**
     * 用于设置{@linkplain java.sql.Statement#setQueryTimeout(int)}，单位是秒,默认为-1表示不设置。
     *
     * @return
     */
    int queryTimeoutSeconds() default -1;

    /**
     * 取值为true、且在porter中调用，则事务的提交在整个请求结束后进行
     *
     * @return
     */
    boolean endCommit() default true;

}
