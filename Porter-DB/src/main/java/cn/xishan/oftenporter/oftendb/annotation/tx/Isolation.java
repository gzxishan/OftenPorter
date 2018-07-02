package cn.xishan.oftenporter.oftendb.annotation.tx;

import java.sql.Connection;

/**
 * 事务隔离级别.
 * 另见{@linkplain Connection#TRANSACTION_READ_UNCOMMITTED},{@linkplain Connection#TRANSACTION_READ_COMMITTED},
 * * {@linkplain Connection#TRANSACTION_REPEATABLE_READ},{@linkplain Connection#TRANSACTION_SERIALIZABLE}
 *
 * @author Created by https://github.com/CLovinr on 2018/7/2.
 */
public enum Isolation
{
    DEFAULT(-1),
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);
    private int level;

    Isolation(int level)
    {
        this.level = level;
    }

    public int getLevel()
    {
        return level;
    }
}
