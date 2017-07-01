package cn.xishan.oftenporter.oftendb.db.mysql;

import cn.xishan.oftenporter.oftendb.db.TransactionConfig;

import java.sql.Connection;

/**
 * @author Created by https://github.com/CLovinr on 2017/7/1.
 */
public class SqlTransactionConfig implements TransactionConfig
{
    /**
     * 设置事务级别，为null表示默认的。
     * 见{@linkplain Connection#TRANSACTION_READ_UNCOMMITTED},{@linkplain Connection#TRANSACTION_READ_COMMITTED},
     * {@linkplain Connection#TRANSACTION_REPEATABLE_READ},{@linkplain Connection#TRANSACTION_SERIALIZABLE}
     */
    public Integer transactionLevel;

    public SqlTransactionConfig()
    {

    }
}
