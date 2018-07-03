package cn.xishan.oftenporter.oftendb.db.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * @author Created by https://github.com/CLovinr on 2018/7/1.
 */
public interface IConnection
{
    Connection getConnection();

    void setQueryTimeoutSeconds(int queryTimeoutSeconds);

    /**
     * 防止重复开启事务
     *
     * @return
     */
    boolean willStartTransactionOk();

    void startTransactionOk();

    boolean willCommit();

    void doCommit() throws SQLException;

    /**
     * 返回事务是否结束。
     * @param savepoint
     * @return
     * @throws SQLException
     */
    boolean doRollback(Savepoint savepoint) throws SQLException;

    void doRollback() throws SQLException;
}
