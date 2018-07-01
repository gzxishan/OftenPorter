package cn.xishan.oftenporter.oftendb.annotation;

import cn.xishan.oftenporter.oftendb.db.sql.TransactionJDBCHandle;
import cn.xishan.oftenporter.oftendb.mybatis.MyBatisOption;
import cn.xishan.oftenporter.porter.core.annotation.AspectOperationOfNormal;
import com.sun.org.apache.bcel.internal.generic.RET;

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
     * 设置事务级别。
     * 另见{@linkplain Connection#TRANSACTION_READ_UNCOMMITTED},{@linkplain Connection#TRANSACTION_READ_COMMITTED},
     * {@linkplain Connection#TRANSACTION_REPEATABLE_READ},{@linkplain Connection#TRANSACTION_SERIALIZABLE}
     */
    enum Level
    {
        DEFAULT(-1),
        READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
        READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
        REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
        SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);
        private int level;

        Level(int level)
        {
            this.level = level;
        }

        public int getLevel()
        {
            return level;
        }
    }

    Level level() default Level.DEFAULT;

    boolean readonly() default false;

    String dbSource() default MyBatisOption.DEFAULT_SOURCE;

    String type() default "mybatis";
}
