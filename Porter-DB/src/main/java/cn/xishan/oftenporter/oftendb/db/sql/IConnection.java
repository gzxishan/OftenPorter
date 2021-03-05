package cn.xishan.oftenporter.oftendb.db.sql;

import cn.xishan.oftenporter.oftendb.annotation.tx.Isolation;
import cn.xishan.oftenporter.oftendb.annotation.tx.Readonly;

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

    void setReadonly(Readonly readonly) throws SQLException;

    void setLevel(Isolation level) throws SQLException;

    /**
     * 防止重复开启事务
     *
     * @return
     */
    boolean willStartTransactionOk();

    void startTransactionOk();

    /**
     * 传入框架的连接已经开启了事务
     * @return
     * @throws SQLException
     */
    boolean alreadyOpenTransaction() throws SQLException;

    boolean finishedUntilAlreadyOpenTransaction();

    boolean willCommit();

    void doCommit() throws SQLException;

    /**
     * 返回事务是否结束。
     *
     * @param savepoint
     * @return
     * @throws SQLException
     */
    boolean doRollback(Savepoint savepoint) throws SQLException;

    void doRollback() throws SQLException;
}
